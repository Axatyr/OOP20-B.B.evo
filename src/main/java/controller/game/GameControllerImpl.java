package controller.game;

import java.util.Arrays;
import controller.leaderboard.LeaderboardController;
import controller.leaderboard.LeaderboardControllerImpl;
import controller.settings.SettingsControllerImpl;
import model.entities.Ball;
import model.entities.GameBoard;
import model.entities.GameBoardImpl;
import model.entities.Paddle;
import model.entities.Wall;
import model.leaderboard.Player;
import model.leaderboard.PlayerImpl;
import model.mapeditor.Level;
import model.settings.SettingLevel;
import model.settings.SettingLevelManager;
import model.utilities.ObjectInit;
import model.utilities.Difficulty;
import model.utilities.GameUtilities;
import model.utilities.ScreenUtilities;
import model.utilities.Angle;

public class GameControllerImpl implements GameController {

    /**
     * Setting false for load standard level, true for creative mode.
     * Static because is called in other without instantiate any gameController
     */
    private static boolean creativeMode;

    /**
     * Used for setting next level loading in standard level.
     * Setting false for load next level of standard mode, true to load the first level.
     */
    private static boolean standardModeStart;

    private GameStatus phase;
    private final GameBoard board;
    private final Level level;
    private final PlayerImpl player;
    private final SettingsControllerImpl setting;

    public GameControllerImpl() {
        this.phase = GameStatus.START;
        this.setting = new SettingsControllerImpl(GameUtilities.SETTINGS_PATH); 
        final SettingLevel settingLevel =  SettingLevelManager.loadOption();
        if (isCreativeMode() || isStandardModeStart()) {
            this.level = settingLevel.getSelectedLevel();
        } else {
            this.level = new SettingLevel.SettingLevelBuilder().build().getSelectedLevel();
            setStandardModeStart(true);
        }
        this.player = new PlayerImpl(this.getPlayerAlias(), setting.getDifficulty().getInitialScore(), 
                                     setting.getDifficulty().getNumberOfLives(),
                                     setting.getDifficulty().getMaxNumberOfLife());
        this.board = new GameBoardImpl(new Wall(ScreenUtilities.WORLD_WIDTH, ScreenUtilities.WORLD_HEIGHT), this);
        this.board.setBricks(level.getBricks());
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void init() {
        this.baseMultiplier();
        final Paddle.Builder paddleBuilder = new Paddle.Builder();
        this.board.setPaddle(paddleBuilder.position(ObjectInit.PADDLE.getStartPos())
                                        .width(ObjectInit.PADDLE.getInitWidth())
                                        .height(ObjectInit.PADDLE.getInitHeight())
                                        .texturePath(level.getPaddleTexture().getPath())
                                        .build());
        this.board.setBalls(Arrays.asList(new Ball.Builder()
                                            .position(ObjectInit.BALL.getStartPos())
                                            .direction(Angle.MIDDLE_LEFT.getAngleVector().mul(-1)) 
                                            .height(ObjectInit.BALL.getInitHeight())
                                            .width(ObjectInit.BALL.getInitWidth())
                                            .speed(setting.getDifficulty().getBallVelocity())
                                            .path(level.getBallTexture().getPath())
                                            .build()));
        this.phase = GameStatus.PAUSE;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override 
    public GameStatus getPhase() {
        return this.phase;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void setPhase(final GameStatus phase) {
        this.phase = phase;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public GameBoard getBoard() {
        return board;
   }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Level getLevel() {
        return this.level;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public void baseMultiplier() {
        this.setting.getDifficulty().getMultiplyScoreValue();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Player getPlayer() {
        return this.player;
    }

    /**
     * This method allow to get the alias for the partial player.
     * Saved in ranking whit alias zero.
     * @return alias of current player
     */
    private String getPlayerAlias() {
        final LeaderboardController leaderboard = new LeaderboardControllerImpl(GameUtilities.LEADERBOARD_PATH);
        return leaderboard.viewLeaderboard()
                          .entrySet()
                          .stream()
                          .filter(x -> x.getValue() == 0)
                          .map(x -> x.getKey())
                          .findFirst()
                          .get();
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public int getPlayerScore() {
        return player.getScore(); 
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int getLives() {
        return player.getLife();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Difficulty getDifficulty() {
        return setting.getDifficulty();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean isMusicActive() {
       return setting.isMusicEnable();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean isEffectActive() {
        return setting.isSoundFxEnable();
    }

    /**
     * 
     * @return true if creativeMode is enable, false otherwise
     */
    public static boolean isCreativeMode() {
        return creativeMode;
    }

    /**
     * 
     * @param setMode true when creativeMode start, false as default to play standard level
     */
    public static void setCreativeMode(final boolean setMode) {
        creativeMode = setMode;
    }

    /**
     * 
     * @return true if the user are playing the standardMode, false if is playing first level or level custom
     */
    private static boolean isStandardModeStart() {
        return standardModeStart;
    }

    /**
     * 
     * @param start set true after first level to enable the loading for second level, false as default
     */
    private static void setStandardModeStart(final boolean start) {
        standardModeStart = start;
    }
}