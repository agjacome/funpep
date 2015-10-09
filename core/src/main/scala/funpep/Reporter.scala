package funpep

import java.nio.file.Path

import scala.collection.JavaConverters._

import scalaz._
import scalaz.concurrent._
import scalaz.stream._
import scalaz.std.string._
import scalaz.syntax.applicative._
import scalaz.syntax.foldable._
import scalaz.syntax.std.option._
import scalaz.syntax.std.string._

import contrib._
import data._
import util.functions._
import util.types._
import util.ops.foldable._
import util.ops.path._


object Reporter {

  // TODO: Use atto to parse distance matrix lines instead of hardcoded string
  // manipulation. And, is it actually required to load all distance matrix
  // lines into memory?
  def generateCSVFile[A](reference: Path, filtered: Path, parser: FastaParser[A])(csv: Path)(implicit ev: A ⇒ Compound): KleisliP[Path, Unit] = {
    type MLine  = (Int, Sequence.Header, IList[Double])
    type Matrix = IList[MLine]

    // FIXME: Refactor. Do not .toList and call unsafe List ops, use IList and
    // Foldable directly
    def csvLine(line: MLine, matrix: Matrix, ref: Fasta[A], fil: Fasta[A]): Process[Task, String] = {
      def find(header: String): Maybe[Sequence[A]] =
        fil.entries.toIList.find(_.header startsWith header).toMaybe

      def residues(sequence: Sequence[A]): String =
        sequence.residues.mkString(_.code.toString)

      val (index, header, distances) = line

      val distsL = distances.toList
      val toTake = ref.entries.size

      val maxValue       = distsL.updated(index, Double.MinValue).takeRight(toTake).max
      val maxValueIndex  = distsL.indexOf(maxValue, distances.length - toTake)
      val maxValueHeader = matrix.toList.apply(maxValueIndex)._2

      val reportLine = (find(header) |@| find(maxValueHeader)) {
        (s1, s2) ⇒ s""""$header","${residues(s1)}","$maxValueHeader","${residues(s2)}","$maxValue""""
      }

      reportLine.toProcess
    }

    def csvLines(ref: Fasta[A], fil: Fasta[A]): KleisliP[Path, String] = {
      def readM: KleisliP[Path, IList[String]] =
        Clustal.withDistanceMatrixOf(filtered) {
          matrix ⇒ linesR(matrix).map(_.drop(1)) <* matrix.delete
        }

      def parseM(lines: IList[String]): Process[Task, Matrix] = {
        import scalaz.syntax.traverse._

        val parsed = lines.zipWithIndex traverse { line ⇒
          val index = line._2
          val split = IList(line._1.split("\\s+"): _*)

          (split.headMaybe |@| split.drop(1).traverse(_.parseDouble.toMaybe)) {
            (header, distances) ⇒ (index, header, distances)
          }
        }

        parsed.toProcess
      }

      for {
        rawM     ← readM
        parsedM  ← KleisliP[Path, Matrix](_ ⇒ parseM(rawM))
        parsedML ← KleisliP[Path, MLine ](_ ⇒ parsedM.toProcess)
        reportL  ← KleisliP[Path, String](_ ⇒ csvLine(parsedML, parsedM, ref, fil))
      } yield reportL
    }

    def csvContent: KleisliP[Path, String] =
      for {
        ref   ← KleisliP[Path, Fasta[A]](_ ⇒ parser.fromFileW(reference))
        fil   ← KleisliP[Path, Fasta[A]](_ ⇒ parser.fromFileW(filtered))
        lines ← csvLines(ref, fil)
      } yield lines

    def csvHeader: String =
      """"Comparing ID","Comparing Sequence","Reference ID","Reference Sequence","Similarity Percentage""""

    csvContent.mapK[({ type λ[α] = Process[Task, α] })#λ, Unit] {
      _.prepend(csvHeader :: Nil).intersperse("\n").pipe(text.utf8Encode).to(nio.file.chunkW(csv))
    }
  }

  // TODO: Do not hardcode configuration (colors, graphicsType, image size and
  // type [now always PNG]), use a Reader/Kleisli with a new Config object
  // holding those options. Something like:
  //
  //   final case class TreeConfig (
  //     colors:    TreeColorSet ==>> Color,
  //     graphType: PHYLOGENY_GRAPHICS_TYPE,
  //     imageSize: (Int, Int),
  //     imageType: AptxUtil.GraphicsExportType
  //   )
  //
  //   def generateTreeFiles(...): KleisliP[TreeConfig, Unit] = ???
  //
  // It also would be nice to define at least type aliases for that ugly naming
  // style that Forester uses.
  def generateTreeFiles(newick: Path, annotations: String ==>> String)(phyloxml: Path, png: Path): Process[Task, Unit] = {
    import java.awt.Color
    import org.forester.archaeopteryx._
    import org.forester.io.parsers.nhx.NHXParser
    import org.forester.io.writers.PhylogenyWriter
    import org.forester.phylogeny._
    import org.forester.util.ForesterUtil

    // Effectful methods of Forester
    def configuration: Configuration = {
      val conf = new Configuration

      conf.putDisplayColors(TreeColorSet.BACKGROUND, Color.WHITE)
      conf.putDisplayColors(TreeColorSet.BRANCH,     Color.BLACK)
      conf.putDisplayColors(TreeColorSet.TAXONOMY,   Color.BLACK)
      conf.putDisplayColors(TreeColorSet.ANNOTATION, Color.BLACK)
      conf.putDisplayColors(TreeColorSet.SEQUENCE,   Color.BLACK)
      conf.setPhylogenyGraphicsType(Options.PHYLOGENY_GRAPHICS_TYPE.RECTANGULAR)

      conf
    }

    def readPhylogeny: Phylogeny = {
      val phy = PhylogenyMethods.readPhylogenies(new NHXParser, newick.absolute.toFile).head

      phy.iteratorPostorder.asScala foreach { node ⇒
        val name       = node.getName
        val annotation = annotations.lookup(name) | name
        node.setName(annotation)
      }

      phy
    }

    def writePhyloXML(phy: Phylogeny): Unit = {
      val writer = new PhylogenyWriter
      writer.toPhyloXML(Array(phy), 0, phyloxml.absolute.toFile, ForesterUtil.LINE_SEPARATOR)
    }

    def writePNGImage(phy: Phylogeny, conf: Configuration): Unit = {
      AptxUtil.writePhylogenyToGraphicsFile(
        phy, png.absolute.toFile, 1000, 1000, AptxUtil.GraphicsExportType.PNG, conf
      )
    }

    // Forester side-effect hiding inside a scalaz-stream Process
    AsyncP(readPhylogeny) map { phy ⇒
      writePhyloXML(phy)
      writePNGImage(phy, configuration)
    }
  }

}
