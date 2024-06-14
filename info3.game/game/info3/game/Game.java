package info3.game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.JFrame;
import javax.swing.JLabel;

import gal.ast.AST;
import gal.parser.Parser;
import info3.game.graphics.GameCanvas;
import info3.game.sound.RandomFileInputStream;

import info3.game.model.*;
import info3.game.controller.*;


public class Game {

	static Game game;
	static int duration = 1000;

	public static void main(String args[]) throws Exception {
		try {
			System.out.println("Game starting...");
			game = new Game();
			System.out.println("Game started.");
		} catch (Throwable th) {
			th.printStackTrace(System.err);
		}
	}

	JFrame m_frame;
	JLabel m_text;
	GameCanvas m_canvas;
    CanvasListener m_listener;
	Grille m_grille;
	Control m_control;
	

	//Sound m_music;

	Game() throws Exception {
		// creating a cowboy, that would be a model
		// in an Model-View-Controller pattern (MVC)
		m_control = new Control();


		
		m_grille = config("game/info3/game/config.json");
		// creating a listener for all the events
		// from the game canvas, that would be
		// the controller in the MVC pattern
		m_listener = new CanvasListener(this);
		// creating the game canvas to render the game,
		// that would be a part of the view in the MVC pattern
		m_canvas = new GameCanvas(m_listener);

		System.out.println("  - creating frame...");
		Dimension d = new Dimension(1138, 817);
		m_frame = m_canvas.createFrame(d);

		System.out.println("  - setting up the frame...");
		setupFrame();
	}

	//parse the json file to get the configuration of the game
	Grille config(String config_file) {
		//automates = loadAutomate("game/info3/game/model/Automates/"+automate_file);
		int seed;
		int difficulty;
		int decision_time;
		String automate_file;
		List<Automate> automates;
		HashMap<String, Automate> entities_automates = new HashMap<>();

		try {
			Path path = Path.of(config_file);
			String reader =  Files.readString(path);
			JSONObject config = new JSONObject(reader);
			seed = config.getInt("seed");
			difficulty = config.getInt("difficulty");
			decision_time = config.getInt("decision_time");
			automate_file = config.getString("automate_file");
			automates = loadAutomate("game/info3/game/model/Automates/" + automate_file);

			//go through the list "entities" in the json file
			JSONArray entities = config.getJSONArray("entities");
			for (int i = 0; i < entities.length(); i++) {
				JSONObject entity = entities.getJSONObject(i);
				String name = entity.getString("name");
				String auto = entity.getString("automate");
				entities_automates.put(name, getAutomate(auto, automates));
			}

			//create the grid
			Grille grille = new Grille(34, 34, m_control, seed, entities_automates);
			return grille;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	List<Automate> loadAutomate(String filename) {
        List<Automate> automates= new ArrayList<>();

        AST ast = null;
        try {
            ast = (AST) Parser.from_file(filename);
        } catch (Exception e) {e.printStackTrace();}
        Ast2Automaton visitor = new Ast2Automaton(automates);
        automates = (List<Automate>) ast.accept(visitor);
        return automates;

    }

    public Automate getAutomate(String name, List<Automate> automates) {
        for (Automate a : automates) {
            if (a.name.equals(name)) {
                return a;
            }
        }
        return null;
    }
	/*
	 * Then it lays out the frame, with a border layout, adding a label to the north
	 * and the game canvas to the center.
	 */
	private void setupFrame() {

		m_frame.setTitle("Game");
		m_frame.setLayout(new BorderLayout());

		m_frame.add(m_canvas, BorderLayout.CENTER);

		m_text = new JLabel();
		m_text.setText("Tick: 0ms FPS=0");
		m_frame.add(m_text, BorderLayout.NORTH);

		// center the window on the screen
		m_frame.setLocationRelativeTo(null);

		// make the vindow visible
		m_frame.setVisible(true);
	}

	/*
	 * ================================================================ All the
	 * methods below are invoked from the GameCanvas listener, once the window is
	 * visible on the screen.
	 * ==============================================================
	 */

	/*
	 * Called from the GameCanvas listener when the frame
	 */
	String m_musicName;

	void loadMusic() {
		m_musicName = m_musicNames[m_musicIndex];
		String filename = "resources/" + m_musicName + ".ogg";
		m_musicIndex = (m_musicIndex + 1) % m_musicNames.length;
		try { 
			RandomAccessFile file = new RandomAccessFile(filename,"r");
			RandomFileInputStream fis = new RandomFileInputStream(file);
			m_canvas.playMusic(fis, 0, 1.0F);
		} catch (Throwable th) {
			th.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	private int m_musicIndex = 0;
	private String[] m_musicNames = new String[] { "nostalgia" };

	
	private long decision=1000;
	private long Rythme=300;
    private long m_textElapsed;
	private long m_timekey;
	private boolean authorised = true;

	/*
	 * This method is invoked almost periodically, given the number of milli-seconds
	 * that elapsed since the last time this method was invoked.
	 */
	void tick(long elapsed) {
		
		m_timekey += elapsed;
			
			// If the game is authorized, check if it becomes unauthorized
		
			if (this.authorised) {
   	 			if (m_grille.IsAuthorised() == false) {
					m_timekey = 0;
					m_control.step();
					m_grille.resetTouche();
					this.authorised = false;					
				}
				if (m_timekey > decision) {
					m_timekey = 0;
					m_control.step();
					m_grille.resetTouche();
				}
			}
			else{
				if (m_timekey > Rythme) {
					m_timekey = 0;
					m_grille.Authorised_True();
					authorised = true;
				}
			}


			// Update the game grid
			m_grille.tick(elapsed);

			// Update every second
			// the text on top of the frame: tick and fps
			m_textElapsed += elapsed;
			if (m_textElapsed > 1000) {
				m_textElapsed = 0;
				float period = m_canvas.getTickPeriod();
				int fps = m_canvas.getFPS();

				String txt = "Tick=" + period + "ms";
				while (txt.length() < 15)
					txt += " ";
				txt = txt + fps + " fps   ";
				m_text.setText(txt);
				
			}

			// Update the time key and check if it exceeds the rhythm
			
			
			
		}
	
		
  
      
	


	/*
	 * This request is to paint the Game Canvas, using the given graphics. This is
	 * called from the GameCanvasListener, called from the GameCanvas.
	 */
	void paint(Graphics g) {

		// get the size of the canvas
		int width = m_canvas.getWidth();
		int height = m_canvas.getHeight();

		// erase background
		g.setColor(Color.gray);
		g.fillRect(0, 0, width, height);

		// paint
		m_grille.paint(g, width - 340, height);
		
	}

}
