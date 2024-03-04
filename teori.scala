// Scala
//> using dep org.typelevel::cats-effect::3.5.3

package teori

import cats.effect.IO

// Konstante verdier
val ikkeMuterende = 10
var muterende = 10

def void =
  muterende = 11
  // ikkeMuterende = 10 // compiler ikke

/* Funksjoner */
def funksjonsnavn(parameter: String): ReturType = s"foo$parameter"
type ReturType = String // Type alias

/* Funksjoner er verdier  */
val funksjon: String => ReturType = parameter => s"foo$parameter"

/* classes: Oppskrift på objekt instanser */
class Person(
    val fornavn: String = "Magnus", // Default verdi
    val etternavn: String, // implisitt public
    alder: Int // implisitt private
):
  // metode på en klasse
  def getAge = this.alder + 10

/* Instansiering av nye objekter (trenger ikke new i Scala 3) */
val person = Person("Kristian", "Alvestad Nedrevold-Hansen", 22)

val navn = person.fornavn
// val alder = person.alder // compiler ikke
val alder = person.getAge

/* Objekter - Singleton klasse instanser (statics) */
object Foo: // Bare en av meg
  def foo = "foo" // og meg

/* Companion Objects */
object Person:
  def default = Person("Magnus", "Magnussen", 20)

val defaultPerson = Person.default // static funksjon i Person namespace

/* Case classes
 * Som klasser men med copy, unapply, equals, hashCode og toString generert */
case class Systemutvikler(hodepine: Boolean)

/* traits
 * Samme som interfaces, en kontrakt */

trait BeMakeCode:
  def finishTask(dev: Systemutvikler): Unit

/* Higher Kinded Types - Type Constructors */
trait HigherKinded[K[_]]:
  def foo: K[String]

def higherKindedOption = new HigherKinded[Option]:
  override def foo: Option[String] = Some("foo")

def higherKindedIO = new HigherKinded[IO]:
  override def foo: IO[String] = IO.pure("foo")
