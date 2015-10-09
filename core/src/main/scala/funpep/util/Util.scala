package funpep

package object util {

  object functions extends Functions
  object parsers   extends Parsers
  object types     extends Types

  object ops {
    object disjunction extends ToDisjunctionOps
    object foldable    extends ToFoldableOps
    object ilist       extends ToIListOps
    object path        extends ToPathOps
    object string      extends ToStringOps


    object all extends ToDisjunctionOps
                  with ToFoldableOps
                  with ToIListOps
                  with ToPathOps
                  with ToStringOps
  }

  object all extends Functions
                with Parsers
                with Types
                with ToDisjunctionOps
                with ToFoldableOps
                with ToIListOps
                with ToPathOps
                with ToStringOps

}
