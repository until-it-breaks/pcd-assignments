# Odds-and-Evens Game Implementation

## 1. Problem Analysis
The assignment requires implementing a tournament-style Odds-and-Evens game for `n` = $2^m$ players using the Go language. The primary challenge from a concurrent perspective is managing the synchronization and execution of multiple games simultaneously without relying on shared memory. 

The key concurrent aspects are:

* The tournament must proceed in `m` stages, where all matches in a current round must finish before the next round can begin.
* Matches within a single round are independent and should run concurrently.
* The `Referee` coordinates players and collect results safely using message passing instead of shared state.

## 2. General Strategy
The implementation follows a **centralized coordination strategy** where a `Referee` manages the tournament and individual `Player` goroutines.

1. At the start, `n` player goroutines are spawned. Each player runs a persistent loop, blocked on a channel, waiting for a signal to randomly generate a move (throw either 1 or 2).
2. The `Referee` iterates through `m` rounds. In each round, players are paired off (player 0 vs player 1, player 2 vs player 3, etc.). Each match is run in its own goroutine.
3. The `Referee` uses a buffered `Player` channel to collect the winners of each match. Once the channel collected all the winners of the current round, the `Referee` updates the player list for the next round.
4. This cycle continues until only one undefeated player remains, at which point the `Referee` declares them the tournament winner.

To adhere to the "no shared memory" requirement, all coordination is handled via Go channels:

* The Referee sends a boolean signal to a player's `in` channel to request a move.
* The player sends back a `Move` struct (containing their ID and chosen value) back to the Referee through an `out` channel.
* Communication is also used for termination. When a player is eliminated, the Referee closes their `in` channel. The player goroutine detects this closure and exits its loop.