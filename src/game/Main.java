package game;

import javafx.application.Application;
import javafx.stage.Stage;
import ui.MainMenu;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        MainMenu menu = new MainMenu(stage);
        menu.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
