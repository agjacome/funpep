package funpep

import java.util.UUID


package object util {

  def randomUUID: UUID = UUID.randomUUID

  object ops {
    object foldable extends ToFoldableOps
    object ilist    extends ToIListOps
    object string   extends ToStringOps
  }

}
