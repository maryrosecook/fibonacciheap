(ns fibonacci-heap.test.core
  (:use [fibonacci-heap.core])
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:require [clojure.zip :as z]))

(declare add-children)

(defn nnode [hash] {:key (-> hash :k)})

(defn add-child [node child-hash]
  (z/append-child node (nnode child-hash)))

(defn make-tree [hash node]
  (root-loc
   (if (:k hash)
     (add-children (z/down (add-child node hash))
                   (-> hash :c))
     (add-children node
                   (-> hash :c)))))

(defn add-children [node children-hash]
  (if (empty? children-hash)
    node
    (let [child-hash (first children-hash)
          with-child (add-child node child-hash)
          with-child-children (add-children
                               (-> with-child z/down z/rightmost)
                               (-> child-hash :c))]
      (add-children
       (-> with-child-children z/up)
       (rest children-hash)))))

(defn make-heap [hash]
  (let [loc (make-tree hash (-> (create-heap) :trees))
        heap (update-min (assoc (create-heap) :trees (root-loc loc)))]
    [heap loc]))

;; merge

(deftest test-merge-single-node-tree-to-heap
  (let [data {:a 1}
        key 5
        heap (heap-merge (create-heap) data key)
        node (first (z/children (-> heap :trees)))]
    (is (= (:data node) data))
    (is (= (:key node) key))))

(deftest test-new-merged-node-gets-added-to-end-of-heap
  (let [data-1 {:a 1}
        data-2 {:b 2}
        heap (heap-merge (heap-merge (create-heap) data-1 5) data-2 6)
        node (first (z/children (-> heap :trees)))]
    (is (= (:data node) data-2))))

(deftest test-merge-calls-update-min
  (let [heap (heap-merge (create-heap) {:a 1} 6)]
    (is (= (-> heap :minimum-pointer) 0))))

;; update-minimum

(deftest test-update-min-with-more-than-one-tree
  (let [heap (heap-merge (heap-merge (create-heap) {} 5) {} 6)]
    (is (= (-> heap :minimum-pointer) 1))))

;; find-min

(deftest test-find-min-produces-minimum-node
  (let [data {:a 1}
        heap (heap-merge (create-heap) data 6)
        node (first (z/children (-> heap :trees)))]
    (is (= (:data node) data))))

;; heap-or-tree-root?

(deftest test-heap-or-tree-root?-returns-false-for-a-node
  (let [[_ loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down z/down z/down)]
    (is (not (heap-or-tree-root? loc)))
    (is (not (heap-or-tree-root? (-> loc z/up))))))

(deftest test-heap-or-tree-root?-returns-true-for-real-root
  (let [[_ loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down)]
    (is (heap-or-tree-root? loc))))

;; decrease-key

(deftest test-decreased-key-still-greater-than-parent-when-has-parent
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down z/down z/down)
        new-heap (decrease-key heap loc 9)]
    (is (= (-> new-heap :trees z/down z/down z/down z/node :key)
           9))
    (is (= (-> new-heap :trees z/node :key))
           8)))

(deftest test-decreased-key-when-no-parent
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down z/down z/down)
        new-heap (decrease-key heap (-> loc z/up z/up) 5)]
    (is (= (-> new-heap :trees z/down z/node :key)
           5))))

(deftest test-decreased-key-marks-unmarked-parent-when-key-greater-than-new-key
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down z/down z/down)
        new-heap (decrease-key heap loc 7)]
    (is (-> new-heap :trees z/down z/down z/node :marked))))

(deftest test-decreased-key-when-lt-parent-moves-node-to-top
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down z/down z/down)
        new-heap (decrease-key heap loc 7)]
    (is (= (-> new-heap :trees z/down z/rightmost z/node :key))
        7)))

(deftest test-decreased-key-when-lt-parent-moves-node-and-marked-parent-to-top
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down z/down z/down)
        tree-marked-parent (z/down (z/edit (-> loc z/up)
                                           (fn [node]
                                             (assoc node :marked true))))
        heap-decreased-key (decrease-key heap tree-marked-parent 7)
        right-most-root (-> heap-decreased-key :trees z/down z/rightmost)]
    (is (= (-> right-most-root z/node :key)
           8))
    (is (= (-> right-most-root z/prev z/node :key)
           7))))

(deftest test-decreased-key-when-lt-parent-all-roots-unmarked
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10}]}]})
        loc (-> loc-at-root z/down z/down z/down)
        tree-marked-parent (z/down (z/edit (-> loc z/up)
                                           (fn [node]
                                             (assoc node :marked true))))
        heap-decreased-key (decrease-key heap tree-marked-parent 7)
        right-most-root (-> heap-decreased-key :trees z/down z/rightmost)]
    (is (= (filter (fn [x] (= (:marked x) true))
                   (-> heap-decreased-key :trees z/children)))
           0)))

(deftest test-decreased-key-brings-whole-subtree-of-decreased-node
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8 :c [{:k 10 :c [{:k 12}]}]}]})
        loc (-> loc-at-root z/down z/down z/down)
        heap-decreased-key (decrease-key heap loc 7)
        right-most-root (-> heap-decreased-key :trees z/down z/rightmost)]
    (is (= (-> right-most-root z/node :key)
           7))
    (is (= (-> right-most-root z/down z/node :key)
           12)))) ;; child of cut node

(deftest test-decreased-key-of-non-root-node-updates-min
  (let [[heap loc-at-root] (make-heap {:k 6 :c [{:k 8}]})
        loc (-> loc-at-root z/down z/down)
        heap-decreased-key (decrease-key heap loc 4)]
    (is (= (:key (find-min heap-decreased-key))
           4))))

(deftest test-decreased-key-of-root-node-updates-min
  (let [[heap loc-at-root] (make-heap {:c [{:k 6} {:k 8}]})
        loc (-> loc-at-root z/down z/right)]
    (is (= (:key (find-min heap)
           6)))
    (let [heap-decreased-key (decrease-key heap loc 4)]
      (is (= (:key (find-min heap-decreased-key))
             4)))))

;; extract-min

(deftest test-extract-min-promotes-children-and-removes-node
  (let [[heap loc] (make-heap {:k 6 :c [{:k 8} {:k 10 :c [{:k 12}]}]})
        heap (extract-min heap)]
    (is (= (map (fn [x] (:key x))
                (-> heap :trees z/children))
           [10 8]))))

(deftest test-extract-min-updates-min
  (let [[heap loc] (make-heap {:k 6 :c [{:k 8} {:k 10 :c [{:k 12}]}]})
        heap (extract-min heap)]
    (is (= (:key (find-min heap))
           8))))

(defn get-node [heap commands]
  (reduce (fn [acc x] (x acc))
                 (-> heap :trees)
                 commands))

(defn has-child? [node key]
  (> (count (filter (fn [x] (= (:key x) key))
                    (-> node z/children)))
     0))

(deftest test-extract-min-root-degrees-1-1-1-1
  (let [[h loc-1] (make-heap {:k 10 :c [{:k 100
                                         :c [{:k 1001 :c [{:k 10000}]}]}]})
        [_ loc-2] (make-heap {:k 20 :c [{:k 200
                                         :c [{:k 2001 :c [{:k 20000}]}]}]})
        [_ loc-3] (make-heap {:k 30 :c [{:k 300
                                         :c [{:k 3001 :c [{:k 30000}]}]}]})
        [_ loc-4] (make-heap {:k 40 :c [{:k 400
                                         :c [{:k 4001 :c [{:k 40000}]}]}]})

        roots (map (fn [x] (-> x z/down z/node)) [loc-1 loc-2 loc-3 loc-4])
        heap (assoc h :trees (promote-to-root (create-zipper) roots))
        extracted-heap (extract-min heap)]
    (is (has-child? (get-node extracted-heap [])
                    20))
    (is (has-child? (get-node extracted-heap [z/down])
                    30))
    (is (has-child? (get-node extracted-heap [z/down])
                    40))))

(deftest test-extract-min-root-degrees-3-0-3-2-1-1
  (let [[h loc-1] (make-heap {:k 10 :c [{:k 100 :c [{:k 1000} {:k 1001} {:k 1002}]}]})
        [_ loc-2] (make-heap {:k 20 :c []})
        [_ loc-3] (make-heap {:k 30 :c [{:k 300} {:k 301} {:k 302}]})
        [_ loc-4] (make-heap {:k 40 :c [{:k 400} {:k 401}]})
        [_ loc-5] (make-heap {:k 50 :c [{:k 500}]})
        [_ loc-6] (make-heap {:k 60 :c [{:k 600}]})

        roots (map (fn [x] (-> x z/down z/node)) [loc-1 loc-2 loc-3 loc-4 loc-5 loc-6])
        heap (assoc h :trees (promote-to-root (create-zipper) roots))
        extracted-heap (extract-min heap)]
    (is (has-child? (get-node extracted-heap [])
                    20))
    (is (has-child? (get-node extracted-heap [])
                    30))
    (is (has-child? (get-node extracted-heap [])
                    100))
    (is (has-child? (get-node extracted-heap [z/down z/right])
                    40))
    (is (has-child? (get-node extracted-heap [z/down z/right z/down z/right z/right z/right])
                    50))
    (is (has-child? (get-node
                     extracted-heap
                     [z/down z/right z/down z/right z/right z/right z/down z/right z/right])
                    60))))

;; sanity

(deftest test-get-expected-result-by-appending-child-subtree-to-tree
  (let [existing-hash {:k 0 :c [{:k 10} {:k 11}]}
        additional-hash {:k 100 :c [{:k 1000 :c [{:k 10000} {:k 10001}]}
                                    {:k 1001 :c [{:k 10002} {:k 10003}]}]}
        full-hash (assoc-in existing-hash [:c 1 :c] [additional-hash])

        [_ existing-loc] (make-heap existing-hash)
        existing-loc-at-pos (-> existing-loc z/down z/down z/right)
        [_ additional-loc] (make-heap additional-hash)
        additional-loc-at-pos (-> additional-loc z/down)

        composed-tree-from-fn (z/append-child existing-loc-at-pos
                                              (z/node additional-loc-at-pos))
        [_ composed-tree-from-hash] (make-heap full-hash)]
  (is (= (z/root composed-tree-from-fn)
         (z/root composed-tree-from-hash)))))
