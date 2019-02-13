(ns sliding-puzzle.game
  (:require [sliding-puzzle.render :as render]))

(def puzzle-width 4)
(def puzzle-height 4)

(def ^:private perfect-tails-seq (range 1 (* puzzle-width puzzle-height)))
(def ^:private neighbors [[-1 0] [1 0] [0 -1] [0 1]])

(defn in-range? [x a b]
  (<= a x b))

(defn get-from-matrix [matrix cell]
  (get-in matrix (reverse cell)))

(defn assoc-to-matrix [matrix cell value]
  (assoc-in matrix (reverse cell) value))

(defn in-rect? [x y corner-x corner-y width height]
  (and (in-range? x corner-x (+ corner-x width))
       (in-range? y corner-y (+ corner-y height))))

(defn- pos->tile-num [x y]
  (inc (+ x (* y puzzle-width))))

(defn- finish? [tiles]
  (= perfect-tails-seq
     (take (count perfect-tails-seq) (apply concat tiles))))

(defn- tile-position [screen-x screen-y]
  (let [x (int (/ screen-x render/tile-size))
        y (int (/ screen-y render/tile-size))]
    (when (in-rect? x y 0 0 (dec puzzle-width) (dec puzzle-height))
      [x y])))

(defn- find-near-gap [tile-pos tiles]
  (when tile-pos
    (some (fn [[ox oy]]
            (let [p [(+ (tile-pos 0) ox) (+ (tile-pos 1) oy)]]
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
      (recur xs (+ c (count (filter #(> a %) xs))))
      c)))

(defn- insert-gap-position [tile-seq]
  (let [odd-seq (take (int (/ puzzle-height 2)) (iterate (partial + 2) 1))
        even-seq (take (int (/ puzzle-height 2)) (iterate (partial + 2) 0))
        sum (calc-n-sum tile-seq)
        x (rand-int puzzle-width)
        y (if (even? sum)
            (rand-nth odd-seq)
            (rand-nth even-seq))
        gap-pos (dec (pos->tile-num x y))
        [a b] (split-at gap-pos tile-seq)]
    (concat a [:gap] b)))

(defn- gen-tiles []
  (->> (shuffle perfect-tails-seq)
       insert-gap-position
       (partition 4)
       (map vec)
       vec))

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
