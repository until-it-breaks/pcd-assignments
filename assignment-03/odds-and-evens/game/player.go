package game

import "math/rand"

type Move struct {
	playerId string
	value    int
}

type Player struct {
	Id  string
	in  chan bool
	out chan Move
}

func newPlayer(id string) Player {
	return Player{
		Id:  id,
		in:  make(chan bool),
		out: make(chan Move),
	}
}

func (player Player) Run() {
	for {
		// Wait for signal to make a Move
		_, ok := <-player.in
		// Check if the channel has been closed. If true then exit the loop and stop
		if !ok {
			return
		}
		// Send the move
		player.out <- Move{
			playerId: player.Id,
			value:    rand.Intn(2) + 1, // Randomly choose either 1 or 2
		}
	}
}

func SpawnPlayer(id string) Player {
	player := newPlayer(id)
	go player.Run()
	return player
}
