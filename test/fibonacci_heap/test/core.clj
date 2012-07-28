(ns fibonacci-heap.test.core
  (:use [fibonacci-heap.core])
  (:use [clojure.test])
  (:use [clojure.pprint])
  (:require [clojure.zip :as z]))

(declare add-children)

(defn nnode [hash] {:key (-> hash :k)})

(defn make-tree [hash node]
  (pprint (z/down (add-children (-> hash :c) node)))
  (z/down (add-children (-> hash :c) node)))

(defn add-children [children-hash node]
  (if (empty? children-hash)
    node
    (let [child-hash (first children-hash)]
      ;; (pprint node)
      ;; (pprint (nnode child-hash))
      ;; (pprint (z/append-child node (nnode child-hash)))
      ;; (pprint "woooooo")
      (add-children
       (rest children-hash)
       (z/up (z/append-child node (nnode child-hash)))))))

;; (pprint (make-tree
;;  {:k 6 :c [{:k 8
;;             :c [{:k 10
;;                  :c [{:k 12} {:k 14}]}]}]}
;;  (-> (create-heap) :trees)))

(pprint (make-tree
 {:k 6 :c [{:k 8} {:k 10}]}
 (-> (create-heap) :trees)))

(def tree
  (z/append-child
   (z/append-child
    (z/down (z/append-child (-> (create-heap) :trees)
                            {:key 6}))
    {:key 8})
   {:key 10})
  )

;; (pprint (-> tree z/up))


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

;; ;; update-minimum

;; (deftest test-update-min-with-more-than-one-tree
;;   (let [heap (heap-merge (heap-merge (create-heap) {} 5) {} 6)]
;;     (is (= (-> heap :minimum-pointer) 1))))

;; ;; find-min

;; (deftest test-find-min-produces-minimum-node
;;   (let [data {:a 1}
;;         heap (heap-merge (create-heap) data 6)
;;         node (first (z/children (-> heap :trees)))]
;;     (is (= (:data node) data))))

;; ;; decrease-key

;; (deftest test-decreased-key-still-greater-than-parent-when-has-parent
;;   (let [new-tree (decrease-key tree 9)]
;;     (is (= (-> new-tree z/node :key)
;;            9))
;;     (is (= (-> new-tree z/up z/node :key))
;;            8)))

;; (deftest test-decreased-key-when-no-parent
;;   (let [new-tree (decrease-key (-> tree z/up z/up) 5)]
;;     (is (= (-> new-tree z/node :key)
;;            5))))

;; (deftest test-root?-returns-false-for-a-node
;;   (is (not (heap-or-tree-root? tree)))
;;   (is (not (heap-or-tree-root? (-> tree z/up)))))

;; (deftest test-root?-returns-true-for-real-root
;;   (is (heap-or-tree-root? (-> tree z/up z/up))))

;; (deftest test-decreased-key-marks-unmarked-parent-when-key-greater-than-new-key
;;   (let [new-tree (decrease-key tree 7)]
;;     (is (-> new-tree z/down z/down z/node :marked))))

;; (deftest test-decreased-key-when-lt-parent-moves-node-to-top
;;   (let [new-tree (decrease-key tree 7)]
;;     (is (= (-> new-tree z/down z/rightmost z/node :key))
;;         7)))
