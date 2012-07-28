(ns fibonacci-heap.test.core
  (:use [fibonacci-heap.core])
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:require [clojure.zip :as z]))

;; (declare add-children)

;; (defn nnode [hash] {:key (-> hash :k)})

;; (defn make-tree [hash node]
;;   (add-children (-> hash :c)
;;                 (z/down (z/append-child node (nnode hash)))))

;; (defn add-children [children-hash node]
;;   (if (empty? children-hash)
;;     node
;;     (let [child-hash (first children-hash)]
;;       (add-children
;;        (rest children-hash)
;;        (z/append-child
;;         (add-children (-> child-hash :c) node)
;;         (nnode child-hash))))))

;; ;; (pprint (make-tree
;; ;;  {:k 6 :c [{:k 8
;; ;;             :c [{:k 10
;; ;;                  :c [{:k 12} {:k 14}]}]}]}
;; ;;  (-> (create-heap) :trees)))

;; (pprint (make-tree
;;          {:k 6 :c [{:k 8} {:k 10 :c [{:k 12} {:k 14}]}]}
;;          (-> (create-heap) :trees)))



(def three-tier-heap
  (let [loc (z/down (z/append-child
                     (z/down (z/append-child
                              (z/down (z/append-child
                                       (-> (create-heap) :trees)
                                       {:key 6}))
                              {:key 8}))
                     {:key 10}))
        heap (update-min (assoc (create-heap) :trees (root-loc loc)))]
    [heap loc]))

;; (def four-tier-tree
;;   (z/down (z/append-child three-tier-tree {:key 12})))

;; merge

;; (deftest test-merge-single-node-tree-to-heap
;;   (let [data {:a 1}
;;         key 5
;;         heap (heap-merge (create-heap) data key)
;;         node (first (z/children (-> heap :trees)))]
;;     (is (= (:data node) data))
;;     (is (= (:key node) key))))

;; (deftest test-new-merged-node-gets-added-to-end-of-heap
;;   (let [data-1 {:a 1}
;;         data-2 {:b 2}
;;         heap (heap-merge (heap-merge (create-heap) data-1 5) data-2 6)
;;         node (first (z/children (-> heap :trees)))]
;;     (is (= (:data node) data-2))))

;; (deftest test-merge-calls-update-min
;;   (let [heap (heap-merge (create-heap) {:a 1} 6)]
;;     (is (= (-> heap :minimum-pointer) 0))))

;; update-minimum

(deftest test-update-min-with-more-than-one-tree
  (let [heap (heap-merge (heap-merge (create-heap) {} 5) {} 6)]
    (is (= (-> heap :minimum-pointer) 1))))

;; find-min

;; (deftest test-find-min-produces-minimum-node
;;   (let [data {:a 1}
;;         heap (heap-merge (create-heap) data 6)
;;         node (first (z/children (-> heap :trees)))]
;;     (is (= (:data node) data))))

;; decrease-key

(deftest test-decreased-key-still-greater-than-parent-when-has-parent
  (let [[heap loc] three-tier-heap
        new-tree (decrease-key heap loc 9)]
    (is (= (-> new-tree z/node :key)
           9))
    (is (= (-> new-tree z/up z/node :key))
           8)))

(deftest test-decreased-key-when-no-parent
  (let [[heap loc] three-tier-heap
        new-tree (decrease-key heap (-> loc z/up z/up) 5)]
    (is (= (-> new-tree z/node :key)
           5))))

(deftest test-root?-returns-false-for-a-node
  (let [[heap loc] three-tier-heap]
        (is (not (heap-or-tree-root? loc)))
        (is (not (heap-or-tree-root? (-> loc z/up))))))

(deftest test-root?-returns-true-for-real-root
  (let [[heap loc] three-tier-heap]
    (is (heap-or-tree-root? (-> three-tier-tree z/up z/up)))))

;; (deftest test-decreased-key-marks-unmarked-parent-when-key-greater-than-new-key
;;   (let [new-tree (decrease-key (create-heap)  three-tier-tree 7)]
;;     (is (-> new-tree z/down z/down z/node :marked))))

;; (deftest test-decreased-key-when-lt-parent-moves-node-to-top
;;   (let [new-tree (decrease-key (create-heap)  three-tier-tree 7)]
;;     (is (= (-> new-tree z/down z/rightmost z/node :key))
;;         7)))

;; (deftest test-decreased-key-when-lt-parent-moves-node-to-top
;;   (let [tree-marked-parent (z/down (z/edit (-> three-tier-tree z/up)
;;                                            (fn [node]
;;                                              (assoc node :marked true))))
;;         tree-decreased-key (decrease-key (create-heap) tree-marked-parent 7)
;;         right-most-root (-> tree-decreased-key z/down z/rightmost)]
;;     (is (= (-> right-most-root z/node :key)
;;            8))
;;     (is (= (-> right-most-root z/prev z/node :key)
;;            7))))

;; (deftest test-decreased-key-when-lt-parent-all-roots-unmarked
;;   (let [tree-marked-parent (z/down (z/edit (-> three-tier-tree z/up)
;;                                            (fn [node]
;;                                              (assoc node :marked true))))
;;         tree-decreased-key (decrease-key (create-heap) tree-marked-parent 7)
;;         right-most-root (-> tree-decreased-key z/down z/rightmost)]
;;     (is (= (filter (fn [x] (= (:marked x) true))
;;                    (z/children tree-decreased-key)))
;;            0)))

;; (deftest test-decreased-key-brings-whole-subtree-of-decreased-node
;;   (let [tree (-> four-tier-tree z/up)
;;         tree-decreased-key (decrease-key (create-heap) tree 7)
;;         right-most-root (-> tree-decreased-key z/down z/rightmost)]
;;     (pprint tree-decreased-key)
;;     (is (= (-> right-most-root z/node :key)
;;            7))
;;     (is (= (-> right-most-root z/down z/node :key)
;;            12)))) ;; child of cut node
