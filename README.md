# Fibonacci heap

An implementation of the Fibonacci heap in Clojure.

The Fibonacci heap is faster than a binomial heap when the number of deletions and minimum extractions is asymptotically smaller than the number of insertions, key decreases and merges.

## Usage

### Import the library

    (require '[fibonacci-heap.core :as fib])

### Create a heap

    (def heap (fib/create-heap))

### Add a node

Call `heap-merge`, passing a heap, your node data and the key value for the node.  The function returns the resulting heap.  The node can be any Clojure data type: a string, a map, an integer.  The key is an integer that denotes the sorting order of the node.  A lower key means a higher priority.

    (fib/heap-merge heap {:id 1} 7)

### Find minimum node

Call `find-min` and pass a heap.  The function will return the node data stored at the node with the minimum key.  So, if the node was added as above, in "Add a node", this function will return `{:id 1}`.  It will not return the key.

    (fib/find-min heap)

### Extract (delete) minimum node

`extract-min` removes from the heap the node with the minimum key and returns the new heap.  This is useful for algorithms like Dijkstra, where each node is explored and then discarded.

    (fib/extract-min heap)

### Decrease key

You can traverse your Clojure Fibonacci heap using a zipper (http://clojuredocs.org/clojure_core/clojure.zip).  Zip operations, like `edit`, and movements, like `down` will production data stucture locations, or "locs".  A loc represents being at a certain place in a data structure with a certain state.

`decrease-key` allows you to decrease the key of a node you have traversed your way to.  Call the function and pass a heap, your current zipper loc and the new (lower) key.

    (fib/decrease-key heap loc 5)

There is no increase key operation.

### Search

Fibonacci heaps are not designed to be searched efficiently.  However, this library includes a search that is faster than a linear array walk of the nodes.

Call `search` and pass a heap, a filter function and the key of the node you are looking for.  The filter function should return `true` if it is passed the node data you are looking for, and `false` otherwise.  For example:

    (fib/search heap (fn [node-data] (= 1 (:id node-data)) 5))

## License

Open source, under the MIT license.
