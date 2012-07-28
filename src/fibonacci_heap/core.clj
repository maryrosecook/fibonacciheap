(ns fibonacci-heap.core
  (:require [clojure.zip :as z])
  (:use [clojure.pprint]))

(declare update-min)

(defn mark [loc]
  (z/edit loc #(assoc % :marked true)))

(defn root-loc [loc]
  (if (= :end (loc 1))
    loc
    (let [p (z/up loc)]
      (if p
        (recur p)
        loc))))

(defn heap-or-tree-root? [loc]
  (or (nil? (-> loc z/up))
      (nil? (-> loc z/up z/node :key))))

(defn balance-node
  ([loc] (balance-node loc []))
  ([loc roots]
     (let [new-roots (conj roots (z/node loc))
           parent-loc (if (z/left loc)
                        (-> loc z/remove z/up)
                        (-> loc z/remove))]
       (if (:marked (z/node parent-loc))
         (recur parent-loc new-roots)
         (let [final-parent (if (heap-or-tree-root? parent-loc)
                              parent-loc
                              (mark parent-loc))]
           (reduce z/append-child
                   (root-loc final-parent)
                   (map (fn [x] (assoc x :marked false)) new-roots)))))))

(defn decrease-key [heap loc new-key]
  (let [edited-loc (z/edit
                    loc
                    (fn [node]
                      (assoc node :key new-key)))
        parent (-> edited-loc z/up)]
    (if (heap-or-tree-root? parent)
      edited-loc
      (if (> (-> parent z/node :key)
             (-> edited-loc z/node :key))
        (let [balanced-tree (balance-node edited-loc)
              updated-heap (assoc heap :trees balanced-tree)]
          (if (< new-key (-> balanced-tree (:minimum-pointer heap) :key))
            (update-min updated-heap)
            updated-heap))
        edited-loc))))

(defprotocol IFibonacciHeap
  (heap-merge [this data key])
  (find-min [this])
  (update-min [this])) ;; should be private, really

(defrecord FibonacciHeap [minimum-pointer trees]
  IFibonacciHeap
  (heap-merge [this data key]
    (update-min (assoc this :trees
                       (z/insert-child trees
                                       {:data data :key key :marked false}))))

  (find-min [this]
    (get (z/children trees) minimum-pointer))

  (update-min [this]
    (let [[idx _] (reduce (fn [[s-idx smallest] [x-idx x]]
                            (if (< (:key x) (:key smallest))
                              [x-idx x]
                              [s-idx smallest]))
                          (map vector (range)
                               (z/children trees)))]
      (assoc this :minimum-pointer idx)))
  )

(defn- create-zipper []
  (z/zipper (constantly true)
            :children
            (fn [node new-children]
              (assoc node :children new-children))
            {:children []}))

(defn create-heap []
  (FibonacciHeap. nil (create-zipper)))