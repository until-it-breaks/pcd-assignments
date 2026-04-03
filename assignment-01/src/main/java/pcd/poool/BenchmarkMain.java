package pcd.poool;

import pcd.poool.controller.ai.Bot;
import pcd.poool.controller.commands.CommandQueue;
import pcd.poool.controller.engine.EngineTimeoutEvent;
import pcd.poool.controller.engine.GameEngine;
import pcd.poool.controller.engine.GameEngineListener;
import pcd.poool.controller.engine.GameOverEvent;
import pcd.poool.model.board.Board;
import pcd.poool.model.board.BoardConf;
import pcd.poool.model.board.HolelessBoard;
import pcd.poool.model.board.MassiveBoardConf;
import pcd.poool.model.collision.CollisionResolver;
import pcd.poool.model.collision.ExecutorAccumulatorBasedCollisionResolver;
import pcd.poool.view.View;
import pcd.poool.view.ViewModel;

public class BenchmarkMain {

    public static final int MAX_SIMULATION_TIME = 60;

    public static void main(String[] args) {
        Board board = new Board();
        /**
         * Available collision resolvers:
         * - SequentialCollisionResolver
         * - RawThreadedCollisionResolver
         * - RawThreadedAccumulatorBasedCollisionResolver
         * - ExecutorBasedCollisionResolver
         * - ExecutorAccumulatorBasedCollisionResolver
         */
        CollisionResolver resolver = new ExecutorAccumulatorBasedCollisionResolver();
        BoardConf config = new HolelessBoard(new MassiveBoardConf());
        board.init(config, resolver);

        CommandQueue commandQueue = new CommandQueue();
        View view = new View(new ViewModel(), 1200, 800, commandQueue);

        Bot bot = new Bot(board, commandQueue);
        Thread botThread = new Thread(bot);
        botThread.setName("BotThread");

        GameEngine gameEngine = new GameEngine(commandQueue, board, view, MAX_SIMULATION_TIME);
        gameEngine.addListener(bot);
        gameEngine.addListener(view);
        gameEngine.addListener(new GameEngineListener() {
            @Override
            public void onEngineTimeout(EngineTimeoutEvent event) {
                shutdown();
            }

            @Override
            public void onGameOver(GameOverEvent event) {
                shutdown();
            }

            private void shutdown() {
                AutoCloseable closeable = (AutoCloseable) resolver;
                try {
                    closeable.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Thread gameEngineThread = new Thread(gameEngine);
        gameEngineThread.setName("GameEngineThread");

        System.out.println("Starting benchmark with: " + resolver.getClass().getSimpleName() + " for " + MAX_SIMULATION_TIME + " seconds");

        gameEngineThread.start();
        botThread.start();
    }
}