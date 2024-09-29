package scheduler;

import java.io.IOException;
import java.util.Arrays;

import scheduler.models.GraphModel;
import scheduler.models.StateModel;
import scheduler.parsers.Arguments;
import scheduler.parsers.CLIParser;
import scheduler.parsers.InputOutputParser;
import scheduler.schedulers.DFSScheduler;
import scheduler.schedulers.RoundRobinScheduler;
import scheduler.schedulers.Scheduler;
import scheduler.schedulers.SequentialScheduler;
import scheduler.visualiser.Visualiser;

public class Main {
    public static void main(String[] CLIArguments) throws IOException {
        Arguments arguments;

        try {
            arguments = CLIParser.parseCLIArguments(CLIArguments);
        } catch (Exception exception) {
            CLIParser.displayUsage(exception.getMessage());
            return;
        }

        try {
            GraphModel graph = new GraphModel(arguments.getInputDOTFilePath());

            // RoundRobinScheduler scheduler = new RoundRobinScheduler(graph,
            // arguments.getProcessors());
            // scheduler.schedule();

            // InputOutputParser.outputDOTFile(graph, arguments.getOutputDOTFilePath());

            Scheduler scheduler = new DFSScheduler(graph, arguments.getProcessors());
            StateModel bestState = ((DFSScheduler) scheduler).getSchedule();

//            Scheduler scheduler = new SequentialScheduler(graph, arguments.getProcessors());
//            StateModel bestState = scheduler.getAStarSchedule();
            System.out.println(Arrays.stream(bestState.getFinishTimes()).max().getAsInt());

            System.out.println("Scheduled successfully! Output written to " + arguments.getOutputDOTFilePath());
        } catch (IOException e) {
            System.err.println("ERROR");
            e.printStackTrace();
        }

        Visualiser.run(arguments);
    }
}
