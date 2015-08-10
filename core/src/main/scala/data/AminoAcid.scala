package es.uvigo.ei.sing.funpep
package data

import scalaz._
import scalaz.std.anyVal._
import scalaz.std.string._
import scalaz.syntax.std.option._

sealed abstract class AminoAcid (
  val name: String,
  val code: AminoAcid.Code
)

object AminoAcid {

  type Code = Char

  case object Ala extends AminoAcid("Alanine",        'A')
  case object Arg extends AminoAcid("Arginine",       'R')
  case object Asn extends AminoAcid("Asparagine",     'N')
  case object Asp extends AminoAcid("Aspartic acid",  'D')
  case object Cys extends AminoAcid("Cysteine",       'C')
  case object Glu extends AminoAcid("Glutamic acid",  'E')
  case object Gln extends AminoAcid("Glutamine",      'Q')
  case object Gly extends AminoAcid("Glycine",        'G')
  case object His extends AminoAcid("Histidine",      'H')
  case object Ile extends AminoAcid("Isoleucine",     'I')
  case object Leu extends AminoAcid("Leucine",        'L')
  case object Lys extends AminoAcid("Lysine",         'K')
  case object Met extends AminoAcid("Methionine",     'M')
  case object Phe extends AminoAcid("Phenylalanine",  'F')
  case object Pro extends AminoAcid("Proline",        'P')
  case object Ser extends AminoAcid("Serine",         'S')
  case object Thr extends AminoAcid("Threonine",      'T')
  case object Trp extends AminoAcid("Tryptophan",     'W')
  case object Tyr extends AminoAcid("Tyrosine",       'Y')
  case object Val extends AminoAcid("Valine",         'V')
  case object Sec extends AminoAcid("Selenocysteine", 'U')
  case object Pyl extends AminoAcid("Pyrrolysine",    'O')
  case object Asx extends AminoAcid("Asparagine or aspartic acid", 'B')
  case object Glx extends AminoAcid("Glutamine or glutamic acid",  'Z')
  case object Xle extends AminoAcid("Leucine or isoleucine",       'J')
  case object Xaa extends AminoAcid("Unknown",                     'X')

  implicit val AminoAcidEqual: Equal[AminoAcid] = Equal.equalA
  implicit val AminoAcidShow:  Show[AminoAcid]  = Show.shows(_.code.toString)

  lazy val codes: Code   ==>> AminoAcid = all.map(aa ⇒ (aa.code.toUpper,     aa)).toMap
  lazy val names: String ==>> AminoAcid = all.map(aa ⇒ (aa.name.toUpperCase, aa)).toMap

  def apply(code: Code):   AminoAcid = fromCode(code) | Xaa
  def apply(name: String): AminoAcid = fromName(name) | Xaa

  def fromCode(code: Code): Maybe[AminoAcid] =
    codes.lookup(code.toUpper).toMaybe

  def fromName(name: String): Maybe[AminoAcid] =
    names.lookup(name.toUpperCase).toMaybe

  def all: IList[AminoAcid] = IList[AminoAcid](
    Ala, Arg, Asn, Asp, Cys, Glu, Gln, Gly, His, Ile,
    Leu, Lys, Met, Phe, Pro, Ser, Thr, Trp, Tyr, Val,
    Sec, Pyl, Asx, Glx, Xle, Xaa
  )

}
