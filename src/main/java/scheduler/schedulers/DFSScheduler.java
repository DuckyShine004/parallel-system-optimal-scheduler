package scheduler.schedulers;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;

import java.util.*;

/**
 * This class implements the Depth First Search (DFS) Branch And Bound search algorithm. The algorithm
 * searches all branches in the tree and prunes branches when a better solution is found.
 */
public class DFSScheduler extends Scheduler {
    private int bestFinishTime;

    private Set<StateModel> closedStates;

    /**
     * Constructor for the DFSScheduler class.
     *
     * @param graph represents the graph model.
     * @param processors represents the number of processors for scheduling.
     */
    public DFSScheduler(GraphModel graph, int processors) {
        super(graph, processors);

        this.bestFinishTime = Integer.MAX_VALUE;

        this.closedStates = new HashSet<>();
    }

    public void schedule() {
        schedule(new StateModel(processors, this.numberOfNodes));
    }

    /**
     * Method implements DFS Branch And Bound search algorithm. Recursively schedules each task (state).
     * Branches are pruned if the makespan of the current time exceeds the best finish time.
     *
     * @param state represents the current state of the dfs scheduling algorithm.
     */
    private void schedule(StateModel state) {
        if (state.areAllNodesScheduled()) {
            int maximumFinishTime = state.getMaximumFinishTime();

            if (this.bestFinishTime > maximumFinishTime) {
                this.bestFinishTime = maximumFinishTime;
                setBestState(bestState);
            }

            return;
        }

        if (this.closedStates.contains(state)) {
           return;
        }

        this.closedStates.add(state);

        for (NodeModel node : getAvailableNodes(state)) {
            for (int processor = 0; processor < processors; processor++) {
                StateModel nextState = state.clone();

                int earliestStartTime = getEarliestStartTime(state, node, processor);

                nextState.addNode(node, processor, earliestStartTime);

                if (nextState.getMaximumFinishTime() >= this.bestFinishTime) {
                    continue;
                }

                schedule(nextState);
            }
        }
    }
}