package scheduler.schedulers;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

import java.util.*;

public class DFSScheduler extends Scheduler {
    private int bestFinishTime;

    private StateModel bestState;

    private Set<StateModel> closedStates;

    public DFSScheduler(GraphModel graph, int processors) {
        super(graph, processors);

        this.bestFinishTime = Integer.MAX_VALUE;

        this.bestState = null;

        this.closedStates = new HashSet<>();

        getDFSSchedule(new StateModel(processors, this.numberOfNodes));
    }

    @Override
    public void getDFSSchedule(StateModel state) {
        System.out.println(state.getNumberOfScheduledNodes());

        if (state.areAllNodesScheduled()) {
            int finishTime = Arrays.stream(state.getFinishTimes()).max().getAsInt();
            System.out.println(finishTime);

            if (this.bestFinishTime > finishTime) {
                this.bestFinishTime = finishTime;
                this.bestState = state;
            }

            return;
        }

        if (this.closedStates.contains(state)) {
           return;
        }

        this.closedStates.add(state);

        // For each available node
        for (NodeModel node : getAvailableNodes(state)) {
            // For each processor
            for (int processor = 0; processor < processors; processor++) {
                StateModel nextState = state.clone();

                int earliestStartTime = getEarliestStartTime(state, node, processor);

                nextState.addNode(node, processor, earliestStartTime);

                int thisFinishTime = Arrays.stream(nextState.getFinishTimes()).max().getAsInt();
                if (thisFinishTime >= this.bestFinishTime) {
                    // prune branch
                    continue;
                }

                getDFSSchedule(nextState);
            }
        }
    }

    public StateModel getSchedule() {
        return this.bestState;
    }
}