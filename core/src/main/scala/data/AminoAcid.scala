package funpep
package data

import scalaz._
import scalaz.std.anyVal._
import scalaz.std.string._
import scalaz.syntax.std.option._

sealed abstract class AminoAcid (
  val name: String,
  val code: AminoAcid.Code,
  val mass: Maybe[Double]
)

object AminoAcid {

  import Maybe.{ empty, just }

  type Code = Char

  case object Ala extends AminoAcid("Alanine",        'A', just( 71.0788))
  case object Arg extends AminoAcid("Arginine",       'R', just(156.1875))
  case object Asn extends AminoAcid("Asparagine",     'N', just(114.1039))
  case object Asp extends AminoAcid("Aspartic acid",  'D', just(115.0886))
  case object Cys extends AminoAcid("Cysteine",       'C', just(103.1388))
  case object Gln extends AminoAcid("Glutamine",      'Q', just(128.1307))
  case object Glu extends AminoAcid("Glutamic acid",  'E', just(129.1155))
  case object Gly extends AminoAcid("Glycine",        'G', just( 57.0519))
  case object His extends AminoAcid("Histidine",      'H', just(137.1411))
  case object Ile extends AminoAcid("Isoleucine",     'I', just(113.1594))
  case object Leu extends AminoAcid("Leucine",        'L', just(113.1594))
  case object Lys extends AminoAcid("Lysine",         'K', just(128.1741))
  case object Met extends AminoAcid("Methionine",     'M', just(131.1986))
  case object Phe extends AminoAcid("Phenylalanine",  'F', just(147.1766))
  case object Pro extends AminoAcid("Proline",        'P', just( 97.1167))
  case object Pyl extends AminoAcid("Pyrrolysine",    'O', just(255.3172))
  case object Sec extends AminoAcid("Selenocysteine", 'U', just(150.0388))
  case object Ser extends AminoAcid("Serine",         'S', just( 87.0782))
  case object Thr extends AminoAcid("Threonine",      'T', just(101.1051))
  case object Trp extends AminoAcid("Tryptophan",     'W', just(186.2132))
  case object Tyr extends AminoAcid("Tyrosine",       'Y', just(163.1760))
  case object Val extends AminoAcid("Valine",         'V', just( 99.1326))

  case object Asx extends AminoAcid("Asparagine or aspartic acid", 'B', empty)
  case object Glx extends AminoAcid("Glutamine or glutamic acid",  'Z', empty)
  case object Xle extends AminoAcid("Leucine or isoleucine",       'J', empty)

  case object Xaa extends AminoAcid("Undefined X", 'X', empty)
  case object --- extends AminoAcid("Undefined -", '-', empty)
  case object ___ extends AminoAcid("Undefined _", '_', empty)
  case object ∘∘∘ extends AminoAcid("Undefined .", '.', empty)
  case object Stp extends AminoAcid("Stop codon",  '*', empty)

  implicit val AminoAcidEqual: Equal[AminoAcid] = Equal.equalA
  implicit val AminoAcidShow:  Show[AminoAcid]  = Show.shows(_.code.toString)

  lazy val codes: Code   ==>> AminoAcid = all.map(aa ⇒ (aa.code.toUpper,     aa)).toMap
  lazy val names: String ==>> AminoAcid = all.map(aa ⇒ (aa.name.toUpperCase, aa)).toMap

  def apply(code: Code):   Maybe[AminoAcid] = fromCode(code)
  def apply(name: String): Maybe[AminoAcid] = fromName(name)

  def fromCode(code: Code): Maybe[AminoAcid] =
    codes.lookup(code.toUpper).toMaybe

  def fromName(name: String): Maybe[AminoAcid] =
    names.lookup(name.toUpperCase).toMaybe

  def all: IList[AminoAcid] = IList(
    Ala, Arg, Asn, Asp, Cys, Glu, Gln, Gly, His, Ile,
    Leu, Lys, Met, Phe, Pro, Ser, Thr, Trp, Tyr, Val,
    Sec, Pyl, Asx, Glx, Xle, Xaa, ---, ___, ∘∘∘, Stp
  )

}
