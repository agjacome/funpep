package funpep
package data

import scalaz._

object FastaParser {

  type Fasta[A] = NonEmptyList[Sequence[A]]

}
