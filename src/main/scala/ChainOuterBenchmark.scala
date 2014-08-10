import org.scalameter.Parameters
import org.scalameter.api._
import parsers.ChainsOuterRD
import parsers.truffle.{NonterminalName, ParserState, Tests, UninitializedNonterminalCall}

import scala.collection.mutable

object ChainOuterBenchmark
extends PerformanceTest.OfflineReport {

  val sizes = biggerWarmupset(Gen.enumeration("chainLength")(1, 50, 100, 150, 200))

  def biggerWarmupset[T](t: Gen[T]): Gen[T] = new Gen[T] {
    def warmupset = dataset map generate
    def dataset = t.dataset
    def generate(params: Parameters) = t.generate(params)
  }

  override def reporter = Reporter.Composite(CSVReporter(), RegressionReporter(Tester.Accepter(), Historian.Window(1)), DsvReporter(','), super.reporter)


  performance of "LongChainsOuter" config (
    exec.minWarmupRuns -> 10,
    exec.maxWarmupRuns -> 10000,
    exec.benchRuns -> 100,
    // Just want to run one VM, but the Graal-enabled one with custom flags.
    exec.independentSamples -> 1,
    exec.jvmcmd -> "/home/stefan/opt/graalvm-jdk1.8.0-0.3/bin/java",
    //exec.jvmflags -> "-server -Xss64m -G:+TruffleCompilationExceptionsAreFatal -G:+TraceTruffleInlining -Dtruffle.TraceRewrites=true -Dtruffle.DetailedRewriteReasons=true -G:+TraceTruffleCompilationDetails -G:+TraceTruffleCompilation -G:TruffleCompilationThreshold=1 -XX:+UnlockDiagnosticVMOptions -XX:CompileCommand=print,*::callRoot"
    exec.jvmflags -> "-server -Xss64m -G:+TruffleCompilationExceptionsAreFatal -G:TruffleCompilationThreshold=1"
    ) in {
    val parsers: mutable.Map[Int, (ParserState, NonterminalName)] = mutable.HashMap()

    def setupParser(mode: UninitializedNonterminalCall.CallNodeType) = (chainLength: Int) => {
      parsers getOrElseUpdate(chainLength, {
        UninitializedNonterminalCall.callNodeType = mode
        val parser = new ParserState(Tests.repeat('a', 150))

        val startSymbol = Tests.createChainedProductionsOuter(parser, chainLength)

        println(s"start our warmup loop for $chainLength")
        for (i <- 1 to 10000) {
          parser.resetParserState()
          parser.parse(startSymbol)
        }
        println(s"finished our warmup loop for $chainLength")

        (parser, startSymbol)
      })
    }

    measure method("unoptimized x 10") in {
      using(sizes) setUp {
        setupParser(UninitializedNonterminalCall.CallNodeType.UNOPTIMIZED)
      } in {
        chainLength => {
          val (parser, startSymbol) = parsers(chainLength)
          for (i <- 1 to 10) {
            parser.resetParserState()
            parser.parse(startSymbol)
          }
        }
      }
    }

    measure method("cached x 1000") in {
      using(sizes) setUp {
        setupParser(UninitializedNonterminalCall.CallNodeType.CACHED)
      } in {
        chainLength => {
          val (parser, startSymbol) = parsers(chainLength)
          for (i <- 1 to 1000) {
            parser.resetParserState()
            parser.parse(startSymbol)
          }
        }
      }
    }

    measure method("handwritten x 1000") in {
      val s = Tests.repeat('a', 150)
      using(sizes) in {
        _ => {
          val p = new ChainsOuterRD(s)
          for (i <- 1 to 1000) {
            p.s()
            p.reset()
          }
        }
      }
    }
  }
}