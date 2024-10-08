package scheduler;

import java.io.IOException;

import org.graphstream.graph.Graph;
import scheduler.generator.GraphGenerator;
import scheduler.models.GraphModel;
import scheduler.models.MetricsModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.parallel.ParallelScheduler;
import scheduler.schedulers.sequential.AStarScheduler;
import visualiser.Visualiser;

import static scheduler.constants.Constants.RANDOM_OUTPUT_DOT_FILE_PATH;

/**
 * The Main Class contains the necessary driver code for ensuring our program runs smoothly, and that a valid and
 * optimal schedule is generated. JavaFX code will also run if the user specifies that they want the schedule to be
 * visualised.
 */
public class Main {
    private static Scheduler scheduler;
    private static void runScheduler(Arguments arguments) throws IOException {
//        GraphModel graph = new GraphModel(arguments.getInputDOTFilePath());
        GraphModel graph = GraphGenerator.getRandomGraph();
        String filename = "Random_Graph.dot";
        InputOutputParser.outputDOTFile(graph, RANDOM_OUTPUT_DOT_FILE_PATH.concat(filename));
        //Scheduler schedulerTest = new AStarScheduler(graph, arguments.getProcessors());
        scheduler = new AStarScheduler(graph, arguments.getProcessors());
        long startTimeTest = System.currentTimeMillis();
//        schedulerTest.schedule();
        scheduler.schedule();
        long endTimeTest = System.currentTimeMillis();
        double elapsedTimeTest = (endTimeTest - startTimeTest) / 1000.0;

//        MetricsModel metricsTest = schedulerTest.getMetrics();
//        metricsTest.setElapsedTime(elapsedTimeTest);

//        metricsTest.display();
        GraphGenerator.setNumberOfProcessors(arguments.getProcessors());
        GraphGenerator.displayGraphInformation();
//        Scheduler scheduler = new AStarScheduler(graph, arguments.getProcessors());

        for (int i = 1; i <= 8; i++) {
            arguments.setCores((byte) i);
            Scheduler scheduler = new ParallelScheduler(graph, arguments.getProcessors(), arguments.getCores());

            long startTime = System.currentTimeMillis();
            scheduler.schedule();
            long endTime = System.currentTimeMillis();
            double elapsedTime = (endTime - startTime) / 1000.0;

            MetricsModel metrics = scheduler.getMetrics();
            metrics.setElapsedTime(elapsedTime);

            metrics.display();
        }


//        StateModel bestState = metrics.getBestState();
//            graph.setNodesAndEdgesForState(bestState);
//
//            InputOutputParser.outputDOTFile(graph, arguments.getOutputDOTFilePath());
        arguments.displayOutputDOTFilePath();
    }

//    private void runParallelScheduler(Graph graph, Arguments arguments, boolean isGraphRandom) {
//    }
//
//    private void runSequentialScheduler() {
//
//    }

    /**
     * The main method for executing the main driver code.
     *
     * @param CLIArguments CLIArguments the arguments passed by the user
     */
    public static void main(String[] CLIArguments){
        Arguments arguments;

        try {
            arguments = CLIParser.parseCLIArguments(CLIArguments);
        } catch (Exception exception) {
            CLIParser.displayUsage(exception.getMessage());
            return;
        }

        try {
            runScheduler(arguments);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (arguments.isVisualiseSearch()) {
            Visualiser.run(arguments, scheduler);
        }
        Visualiser.run(arguments, scheduler);
    }
    
}
