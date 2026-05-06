package game

import (
	"fmt"
)

type Referee struct {
	players []Player
}

func NewReferee(players []Player) Referee {
	return Referee{players: players}
}

func (referee Referee) StartTournament() Player {
	currentPlayers := referee.players
	round := 1
	// Single-elimination loop: continue until only one undefeated player remains
	for len(currentPlayers) > 1 {
		fmt.Printf("--- Round %d Start (%d players) ---\n", round, len(currentPlayers))
		currentPlayers = referee.runRound(currentPlayers)
		round++
	}
	close(currentPlayers[0].in)
	// Return the tournament winner
	return currentPlayers[0]
}

func (referee Referee) runRound(players []Player) []Player {
	matchCount := len(players) / 2
	// Used to store the winners to be returned for the next round
	winners := make([]Player, matchCount)
	// A buffered channel to collect winners from concurrent matches. Ensures goroutines don't block when sending results
	matchResults := make(chan Player, matchCount)

	// This loop starts matches in a specific order (0 vs 1, 2 vs 3, etc.)
	for i := 0; i < matchCount; i++ {
		go func(p1, p2 Player) {
			// Once a match finishes, its winner is pushed into the channel
			matchResults <- referee.playMatch(p1, p2)
		}(players[i*2], players[i*2+1])
	}

	for i := 0; i < matchCount; i++ {
		// The first match to finish will be the first to be read here
		// winners[0] will be the winner of the fastest match, not necessarily the winner of Match 0
		winners[i] = <-matchResults
	}
	return winners
}

func (referee Referee) playMatch(p1 Player, p2 Player) Player {
	// Signal the players to make a move
	p1.in <- true
	p2.in <- true
	// Wait for their move
	m1 := <-p1.out
	m2 := <-p2.out
	// Evaluate the result
	sum := m1.value + m2.value
	var winner, loser Player
	if sum%2 == 0 {
		winner, loser = p1, p2
	} else {
		winner, loser = p2, p1
	}
	fmt.Printf("[%s (Even) vs %s (Odd)] | (%d - %d) | Sum: %d | Winner: %s\n", p1.Id, p2.Id, m1.value, m2.value, sum, winner.Id)
	close(loser.in)
	return winner
}
