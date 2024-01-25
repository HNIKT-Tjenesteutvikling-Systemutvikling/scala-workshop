package no.hnikt
package routes

import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.effect.IO
import tyrian.*
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import org.http4s.Response
import org.typelevel.log4cats.LoggerFactory
import no.hnikt.templates.Item
import cats.effect.kernel.Ref
import cats.effect.std.UUIDGen

private[routes] def htmlResponse(html: tyrian.Html[Nothing]): IO[Response[IO]] =
  htmlResponse(html.toString)

private[routes] def htmlResponse(html: String): IO[Response[IO]] =
  Ok(html).map(_.withContentType(`Content-Type`(MediaType.text.html)))

def index(
    items: Ref[IO, List[Item]],
    activeFilter: Ref[IO, templates.ActiveFilter]
)(using
    loggerFactory: LoggerFactory[IO]
): HttpRoutes[IO] =
  val logger = loggerFactory.getLogger

  HttpRoutes.of[IO]:
    case GET -> Root =>
      items.get.flatMap(it => htmlResponse(templates.indexPage(it)))

    case req @ POST -> Root / "newTodo" =>
      for
        data <- req.as[String].map(_.split("=").last)
        uuid <- UUIDGen[IO].randomUUID
        updatedItems <- items.updateAndGet(li => li :+ Item(uuid, data, false))
        currentFilter <- activeFilter.get
        res <- htmlResponse(
          templates.itemList(updatedItems, currentFilter, true)
        )
        _ <- logger.info(updatedItems.map(_.toString).mkString(", "))
      yield (res)

    case req @ POST -> Root / "changeStatus" =>
      for
        data <- req.as[String].map(_.split("=").last)
        updatedItems <- items.updateAndGet(_.map: item =>
          if item.id.toString == data then
            item.copy(completed = !item.completed)
          else item)
        currentFilter <- activeFilter.get
        res <- htmlResponse(templates.itemList(updatedItems, currentFilter))
      yield res

    case GET -> Root / "clearCompleted" =>
      for
        currentFilter <- activeFilter.get
        res <- items
          .updateAndGet(_.filter(!_.completed))
          .flatMap(it =>
            htmlResponse(
              if it.isEmpty then templates.todoBody(true)
              else templates.itemList(it, currentFilter)
            )
          )
      yield res

    case GET -> Root / "getCompleted" =>
      for
        updatedFilter <- activeFilter.updateAndGet(_ =>
          templates.ActiveFilter.Completed
        )
        res <- items.get
          .flatMap: it =>
            val tmp = it.filter(_.completed)
            htmlResponse(
              if tmp.isEmpty then templates.itemList(it, updatedFilter)
              else templates.itemList(tmp, updatedFilter)
            )
      yield (res)

    case GET -> Root / "getActive" =>
      for
        updatedFilter <- activeFilter.updateAndGet(_ =>
          templates.ActiveFilter.Active
        )
        res <- items.get
          .flatMap: it =>
            val tmp = it.filter(!_.completed)
            htmlResponse(
              if tmp.isEmpty then templates.todoBody(false)
              else templates.itemList(tmp, updatedFilter)
            )
      yield res

    case GET -> Root / "getAll" =>
      for
        updatedFilter <- activeFilter.updateAndGet(_ =>
          templates.ActiveFilter.All
        )
        res <- items.get
          .flatMap: it =>
            htmlResponse(
              if it.isEmpty then templates.todoBody(false)
              else templates.itemList(it, updatedFilter)
            )
      yield res

    case GET -> Root / "toggleAll" =>
      for
        currentFilter <- activeFilter.get
        res <- items
          .updateAndGet: items =>
            if items.forall(_.completed) then
              items.map(_.copy(completed = false))
            else items.map(_.copy(completed = true))
          .flatMap: it =>
            htmlResponse(
              if it.isEmpty then templates.todoBody(false)
              else templates.itemList(it, currentFilter)
            )
      yield res

    case req @ POST -> Root / "editItem" =>
      for
        data <- req.as[String].map(_.split("=").last)
        updatedItems <- items.updateAndGet(_.map: item =>
          if item.id.toString == data then item.copy(editable = true)
          else item)
        currentFilter <- activeFilter.get
        res <- htmlResponse(templates.itemList(updatedItems, currentFilter))
      yield res

    case req @ POST -> Root / "updateItem" =>
      for
        data <- req
          .as[String]
          .map(_.split("&").map(_.split("=").last.trim).toList)
        _ <- logger.info(s"\n\n ${data.mkString(",")}")
        i <- items.updateAndGet: it =>
          it.map: item =>
            if item.id.toString == data.last then
              item.copy(name = data.head, editable = false)
            else item
        currentFilter <- activeFilter.get
        res <- htmlResponse(templates.itemList(i, currentFilter))
      yield res

    case req @ POST -> Root / "destroy" =>
      for
        data <- req.as[String].map(_.split("=").last)
        updatedItems <- items.updateAndGet(_.filter: item =>
          item.id.toString != data)
        currentFilter <- activeFilter.get
        res <- htmlResponse(
          if updatedItems.isEmpty then templates.todoBody(true)
          else templates.itemList(updatedItems, currentFilter)
        )
      yield res
