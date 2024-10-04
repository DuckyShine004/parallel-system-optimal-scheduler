package scheduler.schedulers.sequential;

import java.util.*;

import scheduler.models.GraphModel;
import scheduler.models.NodeModel;
import scheduler.models.StateModel;
import scheduler.schedulers.Scheduler;

import static scheduler.constants.Constants.INF_32;

public class AStarScheduler extends Scheduler {
    private final PriorityQueue<StateModel> openedStates;

    private StateModel validState;

    public AStarScheduler(GraphModel graph, int processors) {
        super(graph, processors);

        this.openedStates = new PriorityQueue<>(Comparator.comparingInt(this::getFCost));

        this.validState = getValidSchedule();
    }

    @Override
    public void schedule() {
        boolean isBestStateFound = false;

        this.openedStates.add(new StateModel(processors, numberOfNodes));

        while (!this.openedStates.isEmpty()) {
            StateModel currentState = this.openedStates.poll();

            if (currentState.areAllNodesScheduled()) {
                metrics.setBestState(currentState);
                isBestStateFound = true;

                break;
            }

            closedStates.add(currentState);

            for (NodeModel node : getAvailableNodes(currentState)) {
                for (int processor = 0; processor < processors; processor++) {
                    expandState(currentState, node, processor);
                }
            }
        }

        // If the best state is not found, then the most optimal state has to be the valid state
        if (!isBestStateFound) {
            metrics.setBestState(this.validState);
        }

        metrics.setNumberOfClosedStates(closedStates.size());
    }


    private void expandState(StateModel state, NodeModel node, int processor) {
        // Skip tasks that are not in the fixed order defined
        if (!isFirstAvailableNode(state, node)) {
            return;
        }

        StateModel nextState = state.clone();

        int earliestStartTime = getEarliestStartTime(state, node, processor);

        nextState.addNode(node, processor, earliestStartTime);

        if (!canPruneState(nextState)) {
            this.openedStates.add(nextState);

            metrics.incrementNumberOfOpenedStates();
        }
    }

    private boolean isFirstAvailableNode(StateModel state, NodeModel node) {
        List<NodeModel> equivalentNodeGroup = graph.getEquivalentNodeGroup(node.getGroupId());

        for (NodeModel equivalentNode : equivalentNodeGroup) {
            // both tasks are both the same, so we can continue with scheduling it
            if (equivalentNode.equals(node)) {
                return true;
            }

            // if there is an earlier equivalent task that is not scheduled
            if (!state.isNodeScheduled(equivalentNode)) {
                return false;
            }
        }

        return true;
    }

    private StateModel getValidSchedule() {
        StateModel state = new StateModel(processors, numberOfNodes);

        // for each node the topologically sorted list
        for (NodeModel node : nodes) {
            // for each processor
            int bestStartTime = INF_32;
            int processorWithBestStartTime = -1;

            for (int processor = 0; processor < processors; processor++) {
                int earliestStartTime = getEarliestStartTime(state, node, processor);

                if (earliestStartTime < bestStartTime) {
                    bestStartTime = earliestStartTime;
                    processorWithBestStartTime = processor;
                }
            }

            state.addNode(node, processorWithBestStartTime, bestStartTime);
        }

        this.openedStates.add(state);

        return state;
    }

    public boolean canPruneState(StateModel state) {
        if (closedStates.contains(state)) {
            return true;
        }

        int fCost = getFCost(state);

        if (fCost >= validState.getMaximumFinishTime()) {
            return true;
        }

        return false;
    }

    public int getFCost(StateModel state) {
        if (state.isEmptyState()) {
            return getLowerBound();
        }

        int idleTime = getIdleTime(state);
        int maximumDataReadyTime = getMaximumDataReadyTime(state);
        int maximumBottomLevelPathLength = getMaximumBottomLevelPathLength(state);

        return Math.max(idleTime, Math.max(maximumBottomLevelPathLength, maximumDataReadyTime));
    }

    public int getLowerBound() {
        double loadBalancedTime = (double) graph.getTotalNodeWeight() / processors;

        return (int) Math.max(Math.ceil(loadBalancedTime), getCriticalPathLength());
    }

    public int getIdleTime(StateModel state) {
        double totalWeight = (double) graph.getTotalNodeWeight() + state.getTotalIdleTime();

        return (int) Math.ceil(totalWeight / processors);
    }

    // V2- can be reduced to O(1) but with increased memory? I'll see if it is worth it
    // note: fbl(s)=max(fbl(s_parent),ts(last)+bl(last))
    public int getMaximumBottomLevelPathLength(StateModel state) {
        int maximumBottomLevelPathLength = 0;

        for (NodeModel node : getScheduledNodes(state)) {
            int cost = state.getNodeStartTime(node) + bottomLevelPathLengths[node.getByteId()];
            maximumBottomLevelPathLength = Math.max(maximumBottomLevelPathLength, cost);
        }

        return maximumBottomLevelPathLength;
    }

    // This is actually amortised O(1) from O(|free(s)| * |P|)
    public int getMaximumDataReadyTime(StateModel state) {
        int maximumDataReadyTime = 0;

        for (NodeModel node : getAvailableNodes(state)) {
            int cost = getMinimumDataReadyTime(state, node) + bottomLevelPathLengths[node.getByteId()];
            maximumDataReadyTime = Math.max(maximumDataReadyTime, cost);
        }

        return maximumDataReadyTime;
    }

    public int getMinimumDataReadyTime(StateModel state, NodeModel node) {
        int minimumDataReadyTime = Integer.MAX_VALUE;

        for (int processor = 0; processor < processors; processor++) {
            int dataReadyTime = getEarliestStartTime(state, node, processor);
            minimumDataReadyTime = Math.min(minimumDataReadyTime, dataReadyTime);
        }

        return minimumDataReadyTime;
    }
}
