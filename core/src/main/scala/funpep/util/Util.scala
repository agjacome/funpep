package funpep

package object util {

  object functions extends Functions

  object ops {
    object foldable extends ToFoldableOps
    object ilist    extends ToIListOps
    object path     extends ToPathOps
    object string   extends ToStringOps
  }

  object types extends Types

}
