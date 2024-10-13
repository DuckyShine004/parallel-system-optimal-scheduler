package scheduler.generator;

import static scheduler.constants.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import scheduler.enums.GraphType;
import scheduler.models.GraphModel;
import scheduler.parsers.InputOutputParser;
import scheduler.utilities.Utility;

public class GraphGenerator {
  private static class GraphInformation {
    protected static int numberOfNodes = 0;
    protected static int numberOfEdges = 0;

    protected static byte numberOfProcessors = 0;
  }

  public static GraphModel getRandomGraph() {
    initialiseGraphInformation();

    return new GraphModel(generateRandomGraph());
  }

  private static void initialiseGraphInformation() {
    GraphInformation.numberOfNodes = getRandomNumberOfNodes();
    GraphInformation.numberOfEdges = getRandomNumberOfEdges();
  }

  private static boolean isCyclic(boolean[][] adjacencyMatrix) {
    boolean[] stack = new boolean[GraphInformation.numberOfNodes];
    boolean[] visited = new boolean[GraphInformation.numberOfNodes];

    for (int sourceId = 0; sourceId < GraphInformation.numberOfNodes; sourceId++) {
      if (!visited[sourceId] && isCyclic(sourceId, adjacencyMatrix, visited, stack)) {
        return true;
      }
    }

    return false;
  }

  private static boolean isCyclic(
      int sourceId, boolean[][] adjacencyMatrix, boolean[] visited, boolean[] stack) {
    if (!visited[sourceId]) {
      stack[sourceId] = true;
      visited[sourceId] = true;

      for (int destinationId = 0; destinationId < GraphInformation.numberOfNodes; destinationId++) {
        if (adjacencyMatrix[sourceId][destinationId]) {
          if (!visited[destinationId]) {
            if (isCyclic(destinationId, adjacencyMatrix, visited, stack)) {
              return true;
            }
          } else if (stack[destinationId]) {
            return true;
          }
        }
      }
    }

    stack[sourceId] = false;

    return false;
  }

  private static void addNode(Graph graph, String nodeId) {
    if (graph.getNode(nodeId) == null) {
      Node node = graph.addNode(nodeId);
      node.setAttribute("Weight", getRandomWeight());
    }
  }

  private static void addEdge(Graph graph, String sourceId, String destinationId) {
    String edgeId = sourceId.concat(destinationId);

    if (graph.getEdge(edgeId) == null) {
      Edge edge = graph.addEdge(sourceId.concat(destinationId), sourceId, destinationId, true);
      edge.setAttribute("Weight", getRandomWeight());
    }
  }

  private static Graph generateRandomGraph() {
    Graph graph = new SingleGraph("graph");

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    int[][] edgePermutations = getEdgePermutations();

    boolean[] isSourceNode = getSourceNodePermutations();

    int edgeIndex = 0;
    int edgeCount = 0;

    // Naive DAG generator- improved with 2d array shuffling
    while (edgeIndex < edgePermutations.length && edgeCount < GraphInformation.numberOfEdges) {
      int sourceId = edgePermutations[edgeIndex][0];
      int destinationId = edgePermutations[edgeIndex][1];

      if (isSourceNode[destinationId]) {
        ++edgeIndex;

        continue;
      }

      adjacencyMatrix[sourceId][destinationId] = true;

      if (isCyclic(adjacencyMatrix)) {
        adjacencyMatrix[sourceId][destinationId] = false;
      } else {
        ++edgeCount;
      }

      ++edgeIndex;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);

    return graph;
  }

  /**
   * Creates a graph from an adjacency matrix. The adjacency matrix is used to determine the edges
   * in the graph. The graph is created by iterating through the adjacency matrix and adding nodes
   * and edges to the graph.
   *
   * @param graph
   * @param adjacencyMatrix
   */
  private static void createGraphFromAdjacencyMatrix(Graph graph, boolean[][] adjacencyMatrix) {
    for (int sourceIndex = 0; sourceIndex < GraphInformation.numberOfNodes; sourceIndex++) {
      for (int destinationIndex = 0;
          destinationIndex < GraphInformation.numberOfNodes;
          destinationIndex++) {
        if (adjacencyMatrix[sourceIndex][destinationIndex]) {
          String sourceId = String.valueOf(sourceIndex);
          String destinationId = String.valueOf(destinationIndex);

          addNode(graph, sourceId);
          addNode(graph, destinationId);

          addEdge(graph, sourceId, destinationId);
        }
      }
    }

    GraphInformation.numberOfEdges = graph.getEdgeCount();
  }

  /**
   * Generates a stencil graph with random layers and nodes per layer then outputs the graph to a
   * DOT located in the resources folder.
   *
   * @throws IOException
   */
  public static void createStencilGraphWithRandomLayersAndNodes() throws IOException {
    Graph graph = new SingleGraph("stencil-graph");

    // Get random number of layers and nodes per layer
    int numLayers = getRandomNumberOfLayers();
    int numNodesPerLayer = getRandomNumberOfNodes();

    // Total number of nodes is layers * nodes per layer
    int totalNodes = numLayers * numNodesPerLayer;
    boolean[][] adjacencyMatrix = new boolean[totalNodes][totalNodes];

    // Create nodes for each layer
    int[][] layerNodes = new int[numLayers][numNodesPerLayer];
    for (int layer = 0; layer < numLayers; layer++) {
      for (int nodeIndex = 0; nodeIndex < numNodesPerLayer; nodeIndex++) {
        int actualNodeIndex = (layer * numNodesPerLayer) + nodeIndex;
        layerNodes[layer][nodeIndex] = actualNodeIndex;
        addNode(graph, Integer.toString(actualNodeIndex)); // Add the node to the graph
      }
    }

    // Connect nodes between layers, ensuring each node connects to at least 2 nodes in the next
    // layer.
    for (int layer = 0; layer < numLayers - 1; layer++) {
      for (int nodeIndex = 0; nodeIndex < numNodesPerLayer; nodeIndex++) {
        int currentNode = layerNodes[layer][nodeIndex];

        // Get two random distinct nodes from the next layer
        List<Integer> nextLayerNodes = new ArrayList<>();
        for (int i = 0; i < numNodesPerLayer; i++) {
          nextLayerNodes.add(layerNodes[layer + 1][i]);
        }
        Collections.shuffle(nextLayerNodes);

        // Ensure each node in the current layer is connected to at least 2 nodes in the next.
        for (int i = 0; i < 2; i++) {
          int nextLayerNode = nextLayerNodes.get(i);
          addEdge(graph, Integer.toString(currentNode), Integer.toString(nextLayerNode));
          adjacencyMatrix[currentNode][nextLayerNode] = true;
        }
      }
    }

    // Write the stencil graph to a DOT file
    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.STENCIL));
  }

  // Could refactor into utility class later - formatter class?
  public static void displayGraphInformation() {
    System.out.println("\nGenerated Graph Information:");
    System.out.printf("  %-25s %d%n", "Number of nodes:", GraphInformation.numberOfNodes);
    System.out.printf("  %-25s %d%n", "Number of edges:", GraphInformation.numberOfEdges);
    System.out.printf("  %-25s %d%n", "Number of processors:", GraphInformation.numberOfProcessors);
  }

  private static int getRandomNumberOfNodes() {
    return Utility.getRandomInteger(NUMBER_OF_NODES_LOWER_BOUND, NUMBER_OF_NODES_UPPER_BOUND);
  }

  private static int getRandomNumberOfEdges() {
    double randomPercentage =
        Utility.getRandomPercentage(EDGE_RATIO_LOWER_BOUND, EDGE_RATIO_UPPER_BOUND);

    return (int) (randomPercentage * getMaximumNumberOfDAGEdges());
  }

  private static int getRandomNumberOfSourceNodes() {
    double randomPercentage =
        Utility.getRandomPercentage(SOURCE_NODE_RATIO_LOWER_BOUND, SOURCE_NODE_RATIO_UPPER_BOUND);

    return Math.max(1, (int) (randomPercentage * GraphInformation.numberOfNodes));
  }

  private static int getRandomNumberOfLayers() {
    // Generate a random percentage between a lower and upper bound for layers
    double randomPercentage =
        Utility.getRandomPercentage(LAYER_RATIO_LOWER_BOUND, LAYER_RATIO_UPPER_BOUND);

    // Calculate and return the random number of layers, based on the total number of nodes
    return Math.max(2, (int) (randomPercentage * GraphInformation.numberOfNodes));
  }

  private static int getMaximumNumberOfDAGEdges() {
    return getMaximumNumberOfNonDAGEdges() / 2;
  }

  private static int getMaximumNumberOfNonDAGEdges() {
    return GraphInformation.numberOfNodes * (GraphInformation.numberOfNodes - 1);
  }

  private static double getRandomWeight() {
    return Utility.getRandomInteger(WEIGHT_LOWER_BOUND, WEIGHT_UPPER_BOUND);
  }

  public static void setNumberOfProcessors(byte numberOfProcessors) {
    GraphInformation.numberOfProcessors = numberOfProcessors;
  }

  private static int[][] getEdgePermutations() {
    int edgeIndex = 0;
    int maximumNumberOfEdges = getMaximumNumberOfNonDAGEdges();

    int[][] edgePermutations = new int[maximumNumberOfEdges][2];

    for (int sourceId = 0; sourceId < GraphInformation.numberOfNodes; sourceId++) {
      for (int destinationId = 0; destinationId < GraphInformation.numberOfNodes; destinationId++) {
        if (sourceId != destinationId) {
          edgePermutations[edgeIndex][0] = sourceId;
          edgePermutations[edgeIndex][1] = destinationId;

          ++edgeIndex;
        }
      }
    }

    Utility.shuffle2DArray(edgePermutations, maximumNumberOfEdges);

    return edgePermutations;
  }

  private static boolean[] getSourceNodePermutations() {
    boolean[] isSourceNode = new boolean[GraphInformation.numberOfNodes];

    int sourceNodeIndex = 0;
    int numberOfSourceNodes = 0;
    int maximumNumberOfSourceNodes = getRandomNumberOfSourceNodes();

    while (sourceNodeIndex < GraphInformation.numberOfNodes
        && numberOfSourceNodes < maximumNumberOfSourceNodes) {
      isSourceNode[sourceNodeIndex] = Utility.getRandomBoolean();

      if (isSourceNode[sourceNodeIndex]) {
        ++numberOfSourceNodes;
      }

      ++sourceNodeIndex;
    }

    return isSourceNode;
  }

  public static void createRandomGraphs(int numberOfRandomGraphs) throws IOException {
    for (int graphIndex = 0; graphIndex < numberOfRandomGraphs; graphIndex++) {
      String filename = "Random_%d_Graph.dot".formatted(graphIndex);

      InputOutputParser.writeDOTFile(generateRandomGraph(), filename);
    }
  }

  public static void createForkAndJoinGraph() throws IOException {
    Graph graph = new SingleGraph("graph");

    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 1; nodeId < GraphInformation.numberOfNodes - 1; nodeId++) {
      adjacencyMatrix[0][nodeId] = true;
      adjacencyMatrix[nodeId][GraphInformation.numberOfNodes - 1] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);

    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.FORK_AND_JOIN));
  }

  public static void createForkGraph() throws IOException {
    Graph graph = new SingleGraph("graph");

    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 1; nodeId < GraphInformation.numberOfNodes; nodeId++) {
      adjacencyMatrix[0][nodeId] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);

    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.FORK));
  }

  public static void createJoinGraph() throws IOException {
    Graph graph = new SingleGraph("graph");

    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 0; nodeId < GraphInformation.numberOfNodes - 1; nodeId++) {
      adjacencyMatrix[nodeId][GraphInformation.numberOfNodes - 1] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);

    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.JOIN));
  }

  public static void createIndependentGraph() throws IOException {
    Graph graph = new SingleGraph("graph");

    initialiseGraphInformation();

    for (int nodeId = 0; nodeId < GraphInformation.numberOfNodes; nodeId++) {
      addNode(graph, Integer.toString(nodeId));
    }

    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.INDEPENDENT));
  }

  public static void createChainGraph() throws IOException {
    Graph graph = new SingleGraph("graph");

    initialiseGraphInformation();

    boolean[][] adjacencyMatrix =
        new boolean[GraphInformation.numberOfNodes][GraphInformation.numberOfNodes];

    for (int nodeId = 0; nodeId < GraphInformation.numberOfNodes - 1; nodeId++) {
      adjacencyMatrix[nodeId][nodeId + 1] = true;
    }

    createGraphFromAdjacencyMatrix(graph, adjacencyMatrix);

    InputOutputParser.writeDOTFile(graph, getFilename(GraphType.CHAIN));
  }

  private static String getFilename(GraphType graphType) {
    return getFilenamePrefix(graphType).concat(getFilenameSuffix());
  }

  private static String getFilenamePrefix(GraphType graphType) {
    return switch (graphType) {
      case FORK -> "fork";
      case JOIN -> "join";
      case CHAIN -> "chain";
      case RANDOM -> "random";
      case INDEPENDENT -> "independent";
      case FORK_AND_JOIN -> "fork_and_join";
      case STENCIL -> "stencil";
    };
  }

  private static String getFilenameSuffix() {
    return "_nodes_%d_edges_%d"
        .formatted(GraphInformation.numberOfNodes, GraphInformation.numberOfEdges);
  }
}
