(ns sliding-puzzle.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quil.applet :as a]))

(def tile-size 100)
(def puzzle-width 4)
(def puzzle-height 4)

(defn in-range? [x a b]
  (<= a x b))

(defn get-from-matrix [matrix cell]
  (get-in matrix [(cell 1) (cell 0)]))

(defn assoc-to-matrix [matrix cell value]
  (assoc-in matrix [(cell 1) (cell 0)] value))

(defn in-rect? [x y corner-x corner-y width height]
  (and (in-range? x corner-x (+ corner-x width))
       (in-range? y corner-y (+ corner-y height))))

(defn tile-position [screen-x screen-y]
  (let [x (int (/ screen-x tile-size))
        y (int (/ screen-y tile-size))]
    (when (in-rect? x y 0 0 (dec puzzle-width) (dec puzzle-height))
      [x y])))

(defn find-near-gap [tile-pos tiles]
  (when tile-pos
    (some (fn [[ox oy]]
            (let [p [(+ (tile-pos 0) ox)
                     (+ (tile-pos 1) oy)]]
              (when (= (get-from-matrix tiles p) :gap)
                p)))
          [[-1 0] [1 0] [0 -1] [0 1]])))

(defn swap-tiles [tiles a b]
  (if (and a b)
    (-> tiles
        (assoc-to-matrix a (get-from-matrix tiles b))
        (assoc-to-matrix b (get-from-matrix tiles a)))
    tiles))

(defn initial-state []
  {:tiles [[1 2 3 4]
           [5 6 7 8]
           [9 :gap 11 12]
           [13 14 15 10]]})

(defn draw-tiles [tiles]
  (doseq [x (range 4)
          y (range 4)
          :let [title (get-from-matrix tiles [x y])
                pos-x (* x tile-size)
                pos-y (* y tile-size)]]
    (q/fill 255 255 150)
    (q/rect pos-x pos-y tile-size tile-size)
    (q/text-size 30)
    (q/text-align :center :center)
    (q/fill 0)
    (q/text (str (when-not (= title :gap) title))
            (+ pos-x 50) (+ pos-y 45))))

(defn play [state x y]
  (let [pos (tile-position x y)
        gap (find-near-gap pos (:tiles state))]
    (update state :tiles swap-tiles pos gap)))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  (initial-state))

(defn on-mouse-pressed [state event]
  (let [{:keys [x y]} event]
    (play state x y)))

(defn draw-state [state]
  (q/background 0)
  (draw-tiles (:tiles state)))

(defn update-state [state]
  state)

(q/defsketch sliding-puzzle
  :size [600 401]
  :title "Sliding Puzzle the game"
  :features [:keep-on-top :no-bind-output]
  :setup setup
  :draw draw-state
  :update update-state
  :mouse-pressed on-mouse-pressed
  :middleware [m/fun-mode m/pause-on-error])

(a/with-applet sliding-puzzle
  (q/state))
