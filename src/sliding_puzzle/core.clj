(ns sliding-puzzle.core
  (:require [quil.core :as q]
            [quil.middleware :as m]
            [quil.applet :as a]
            [sliding-puzzle.game :as game]
            [sliding-puzzle.render :as render]))

(defn setup []
  (q/frame-rate 30)
  (q/color-mode :rgb)
  (game/initial-state))

(q/defsketch sliding-puzzle
  :size [401 401]
  :title "Sliding Puzzle the game"
  :features [:keep-on-top :no-bind-output]
  :setup setup
  :draw render/draw-state
  :mouse-pressed game/on-mouse-pressed
  :middleware [m/fun-mode m/pause-on-error])
