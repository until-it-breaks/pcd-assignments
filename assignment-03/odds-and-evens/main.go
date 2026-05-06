package main

import (
	"flag"
	"fmt"
	"main/game"
	"math"
)

func main() {
	var roundCount int
	flag.IntVar(&roundCount, "rounds", 4, "the number of rounds to be played (playercount = 2^rounds)")
	flag.Parse()
	playerCount := int(math.Pow(2, float64(roundCount)))

	players := make([]game.Player, playerCount)
	fmt.Printf("Spawning %d players for %d rounds...\n", playerCount, roundCount)
	for i := 0; i < playerCount; i++ {
		id := fmt.Sprintf("player-%d", i)
		players[i] = game.SpawnPlayer(id)
	}

	referee := game.NewReferee(players)
	winner := referee.StartTournament()
	fmt.Printf("The winner of the tournament is: %s\n", winner.Id)
}
