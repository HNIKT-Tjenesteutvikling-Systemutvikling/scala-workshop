package no.hnikt
package templates

import tyrian.*
import tyrian.Html.*
import tyrian.htmx.*
import tyrian.htmx.Html.*
import org.http4s.Uri
import java.util.UUID

case class Item(
    id: UUID,
    name: String,
    completed: Boolean,
    editable: Boolean = false
)

enum ActiveFilter:
  case All
  case Active
  case Completed

def indexPage(items: List[Item]) =
  "<!DOCTYPE html>" + html(
    head(
      meta(charset := "utf-8"),
      script(src := "https://unpkg.com/htmx.org@1.9.10")(),
      link(
        href := "/assets/index.css",
        rel := "stylesheet"
      )
    ),
    div(
      if items.isEmpty then templates.app(templates.todoBody(true))
      else templates.app(templates.itemList(items, ActiveFilter.All))
    )
  )

def todoBody(lastActionCreateTodo: Boolean) =
  div(id := "todo-list-container")(
    todoHeader(lastActionCreateTodo)
  )

def todoHeader(lastActionCreateTodo: Boolean) = header(`class` := "header")(
  h1("todos"),
  input(
    hxPost := "/newTodo",
    name := "newTodo",
    hxTarget := "#todo-list-container",
    hxSwap := "innerHTML",
    `class` := "new-todo",
    placeholder := "What needs to be done",
    autofocus(lastActionCreateTodo)
  )
)

def itemList(
    items: List[Item],
    activeFilter: ActiveFilter,
    lastActionCreateTodo: Boolean = false
) =
  if items.nonEmpty then
    div(id := "todo-list-container")(
      todoHeader(lastActionCreateTodo),
      main(`class` := "main", style := "display: block;")(
        div(`class` := "toggle-all-container")(
          input(
            `class` := "toggle-all",
            `type` := "checkbox",
            checked(items.filter(!_.completed).isEmpty)
          ),
          label(
            `class` := "toggle-all-label",
            `for` := "toggle-all",
            hxGet := "/toggleAll",
            hxTarget := "#todo-list-container",
            hxTrigger := "click",
            hxSwap := "innerHTML"
          )(
            "Mark all as complete"
          )
        ),
        ul(`class` := "todo-list", id := "todo-list")(
          items.map: item =>
            todoItem(item)
        )
      ),
      footer(`class` := "footer", style := "display: block;")(
        span(`class` := "todo-count")(
          strong(items.filter(!_.completed).size.toString),
          text(" items left")
        ),
        ul(`class` := "filters")(
          li(
            a(
              hxGet := "/getAll",
              hxTarget := "#todo-list-container",
              hxSwap := "innerHTML",
              `class` := (if activeFilter == ActiveFilter.All then "selected"
                          else "")
            )("All"),
            a(
              hxGet := "/getActive",
              hxTarget := "#todo-list-container",
              hxSwap := "innerHTML",
              `class` := (if activeFilter == ActiveFilter.Active then "selected"
                          else "")
            )("Active"),
            a(
              hxGet := "/getCompleted",
              hxTarget := "#todo-list-container",
              hxSwap := "innerHTML",
              `class` := (if activeFilter == ActiveFilter.Completed then
                            "selected"
                          else "")
            )("Completed")
          )
        ),
        if items.filter(_.completed).nonEmpty then
          button(
            `class` := "clear-completed",
            style := "display: block;",
            hxGet := "/clearCompleted",
            hxTarget := "#todo-list-container",
            hxSwap := "innerHTML"
          )(
            "Clear completed"
          )
        else div()
      )
    )
  else div()

def app(todoList: Html[Nothing]) =
  section(`class` := "todoapp")(
    todoList
  )

def todoItem(item: Item) =
  li(
    `class` := (if item.completed then "completed"
                else if item.editable then "editing"
                else ""),
    tyrian.Html.attribute("data-id", item.id.toString)
  )(
    form(
      hxTrigger := "submit"
    )(
      if item.editable then
        div(
          input(
            `class` := "edit",
            name := "value",
            hxPost := "/updateItem",
            hxTarget := "#todo-list-container",
            hxSwap := "innerHTML",
            autofocus(true)
          ),
          input(`type` := "hidden", name := "id", value := item.id.toString)
        )
      else
        div(`class` := "view")(
          input(
            `class` := "toggle",
            `type` := "checkbox",
            checked(item.completed),
            hxPost := "/changeStatus",
            hxTarget := "#todo-list-container",
            hxSwap := "innerHTML"
          ),
          label(
            hxPost := "/editItem",
            hxTrigger := "dblclick",
            hxTarget := "#todo-list-container",
            hxSwap := "innerHTML"
          )(
            Uri.decode(item.name)
          ),
          input(`type` := "hidden", name := "data", value := item.id.toString),
          button(
            `class` := "destroy",
            hxPost := "/destroy",
            hxTarget := "#todo-list-container",
            hxSwap := "innerHTML"
          )
        )
    )
  )
