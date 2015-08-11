package funpep
package data

import scalaz._
import scalaz.std.anyVal._
import scalaz.syntax.std.option._

import atto._
import atto.parser.character.optElem

sealed abstract class AminoAcid (
  val code: Char,
  val name: Maybe[String],
  val mass: Maybe[Double]
) extends Compound[Char]

object AminoAcid {

  import Maybe.{ empty, just }

  case object Ala extends AminoAcid('A', just("Alanine"),        just( 71.0788))
  case object Arg extends AminoAcid('R', just("Arginine"),       just(156.1875))
  case object Asn extends AminoAcid('N', just("Asparagine"),     just(114.1039))
  case object Asp extends AminoAcid('D', just("Aspartic acid"),  just(115.0886))
  case object Cys extends AminoAcid('C', just("Cysteine"),       just(103.1388))
  case object Gln extends AminoAcid('Q', just("Glutamine"),      just(128.1307))
  case object Glu extends AminoAcid('E', just("Glutamic acid"),  just(129.1155))
  case object Gly extends AminoAcid('G', just("Glycine"),        just( 57.0519))
  case object His extends AminoAcid('H', just("Histidine"),      just(137.1411))
  case object Ile extends AminoAcid('I', just("Isoleucine"),     just(113.1594))
  case object Leu extends AminoAcid('L', just("Leucine"),        just(113.1594))
  case object Lys extends AminoAcid('K', just("Lysine"),         just(128.1741))
  case object Met extends AminoAcid('M', just("Methionine"),     just(131.1986))
  case object Phe extends AminoAcid('F', just("Phenylalanine"),  just(147.1766))
  case object Pro extends AminoAcid('P', just("Proline"),        just( 97.1167))
  case object Pyl extends AminoAcid('O', just("Pyrrolysine"),    just(255.3172))
  case object Sec extends AminoAcid('U', just("Selenocysteine"), just(150.0388))
  case object Ser extends AminoAcid('S', just("Serine"),         just( 87.0782))
  case object Thr extends AminoAcid('T', just("Threonine"),      just(101.1051))
  case object Trp extends AminoAcid('W', just("Tryptophan"),     just(186.2132))
  case object Tyr extends AminoAcid('Y', just("Tyrosine"),       just(163.1760))
  case object Val extends AminoAcid('V', just("Valine"),         just( 99.1326))

  case object Asx extends AminoAcid('B', just("Asparagine or aspartic acid"), empty)
  case object Glx extends AminoAcid('Z', just("Glutamine or glutamic acid"),  empty)
  case object Xle extends AminoAcid('J', just("Leucine or isoleucine"),       empty)

  case object Xaa extends AminoAcid('X', empty, empty)
  case object --- extends AminoAcid('-', empty, empty)
  case object ___ extends AminoAcid('_', empty, empty)
  case object ∘∘∘ extends AminoAcid('.', empty, empty)
  case object Stp extends AminoAcid('*', empty, empty)

  implicit val AminoAcidEqual: Equal[AminoAcid] = Equal.equalA
  implicit val AminoAcidShow:  Show[AminoAcid]  = Show.shows(_.code.toString)

  lazy val codes: Char ==>> AminoAcid =
    all.map(aa ⇒ (aa.code → aa)).toMap

  lazy val parser: Parser[AminoAcid] =
    optElem(c ⇒ codes.lookup(c.toUpper))

  def apply   (code: Char): Maybe[AminoAcid] = fromCode(code)
  def fromCode(code: Char): Maybe[AminoAcid] = codes.lookup(code.toUpper).toMaybe

  def all: IList[AminoAcid] = IList(
    Ala, Arg, Asn, Asp, Cys, Glu, Gln, Gly, His, Ile,
    Leu, Lys, Met, Phe, Pro, Ser, Thr, Trp, Tyr, Val,
    Sec, Pyl, Asx, Glx, Xle, Xaa, ---, ___, ∘∘∘, Stp
  )

}
