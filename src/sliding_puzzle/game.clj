(ns sliding-puzzle.game
  (:require [sliding-puzzle.render :as render]))

(def puzzle-width 4)
(def puzzle-height 4)

(def ^:private perfect-tails-seq (range 1 16))
(def ^:private neighbors [[-1 0] [1 0] [0 -1] [0 1]])

(defn pos->index [x y]
  (inc (+ x (* y puzzle-width))))

(defn in-range? [x a b]
  (<= a x b))

(defn get-from-matrix [matrix cell]
  (get-in matrix [(cell 1) (cell 0)]))

(defn assoc-to-matrix [matrix cell value]
  (assoc-in matrix [(cell 1) (cell 0)] value))

(defn in-rect? [x y corner-x corner-y width height]
  (and (in-range? x corner-x (+ corner-x width))
       (in-range? y corner-y (+ corner-y height))))

(defn- valuable-tile-count []
  (dec (* puzzle-width puzzle-height)))

(defn- finish? [tiles]
  (= perfect-tails-seq (take (valuable-tile-count) (apply concat tiles))))

(defn- tile-position [screen-x screen-y]
  (let [x (int (/ screen-x render/tile-size))
        y (int (/ screen-y render/tile-size))]
    (when (in-rect? x y 0 0 (dec puzzle-width) (dec puzzle-height))
      [x y])))

(defn- find-near-gap [tile-pos tiles]
  (when tile-pos
    (some (fn [[ox oy]]
            (let [p [(+ (tile-pos 0) ox)
                     (+ (tile-pos 1) oy)]]
              (when (= (get-from-matrix tiles p) :gap)
                p)))
          neighbors)))

(defn- swap-tiles [tiles p1 p2]
  (if (and p1 p2)
    (-> tiles
        (assoc-to-matrix p1 (get-from-matrix tiles p2))
        (assoc-to-matrix p2 (get-from-matrix tiles p1)))
    tiles))

(defn- check-finish [state]
  (if (finish? (:tiles state))
    (assoc state :mode :finish)
    state))

(defn- play [state x y]
  (let [pos (tile-position x y)
        gap (find-near-gap pos (:tiles state))]
    (-> state
        (update :tiles swap-tiles pos gap)
        (check-finish))))

(defn- calc-n-sum [tiles-seq]
  (loop [[a & xs] tiles-seq
         c 0]
    (if xs
      (recur xs
             (+ c (count (filter #(> a %) xs))))
      c)))

(defn- insert-gap-position [tile-seq]
  (let [odd-seq (take (int (/ puzzle-height 2)) (iterate (partial + 2) 1))
        even-seq (take (int (/ puzzle-height 2)) (iterate (partial + 2) 0))
        sum (calc-n-sum tile-seq)
        col (rand-int puzzle-width)
        row (if (even? sum)
              (rand-nth odd-seq)
              (rand-nth even-seq))
        gap-pos (+ (* row puzzle-width) row)
        [a b] (split-at gap-pos tile-seq)]
    (println "sum:" sum "row:" row)
    (concat a [:gap] b)))

(defn- gen-tiles []
  (let [s (shuffle perfect-tails-seq)]
    (->> (partition 4 (insert-gap-position s))
         (map vec)
         vec)))

(defn initial-state []
  {:tiles (gen-tiles)
   :mode :start})

(defn on-mouse-pressed [state event]
  (let [{:keys [x y]} event]
    (case (:mode state)
      :play (play state x y)
      (:start :finish) (-> (initial-state)
                           (assoc :mode :play))
      state)))
