(ns sliding-puzzle.render
  (:require [quil.core :as q]
            [sliding-puzzle.game :as game]))

(def tile-size 100)

(def ^:private xs (range game/puzzle-width))
(def ^:private ys (range game/puzzle-height))

(defn- draw-tiles [tiles]
  (doseq [x game/xs
          y game/ys
          :let [n (game/get-from-matrix tiles [x y])
                pos-x (* x tile-size)
                pos-y (* y tile-size)
                color (condp = n
                        (game/pos->index x y) [87 213 41]
                        [255 255 150])]]
    (q/stroke-weight 3)
    (q/stroke 30)
    (apply q/fill color)
    (q/rect pos-x pos-y tile-size tile-size)
    (q/text-size 35)
    (q/text-align :center :center)
    (q/fill 30)
    (q/text (str (when-not (= n :gap) n))
            (+ pos-x 50) (+ pos-y 45))))

(defn- draw-start []
  (q/text-size 40)
  (q/fill 255)
  (q/text-align :center :center)
  (q/text "Click to start" 195 170))

(defn- draw-finish []
  (q/text-size 40)
  (q/fill 255)
  (q/text-align :center :center)
  (q/text "You win!" 195 170))

(defn draw-state [state]
  (q/background 0)
  (case (:mode state)
    :play (draw-tiles (:tiles state))
    :start (draw-start)
    :finish (draw-finish)))
