package headmade.god.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;

import net.dermetfan.gdx.graphics.g2d.Box2DSprite;

public class SolarSystem {
	private static final String	TAG			= SolarSystem.class.getName();

	private Body				sun;
	private Array<Integer>		planetScores;
	private Array<Body>			planets		= new Array<Body>();
	private Color				sunColor	= new Color(0xFFE56EFF);
	private float				gravConst	= 1f;

	public SolarSystem(Body sun, Array<Body> planets2) {
		this.sun = sun;
		this.planets = planets2;
		this.planetScores = new Array<Integer>();
		for (int i = 0; i < planets2.size; i++) {
			this.planetScores.add(0);
		}
	}

	public int getScore() {
		Integer totalScore = 0;
		for (final Integer score : planetScores) {
			totalScore += score;
		}
		return totalScore;
	}

	public int update(Body player) {
		final Box2DSprite sunSprite = (Box2DSprite) sun.getUserData();
		sunSprite.setColor(sunColor);

		// planet-sun gravity
		for (final Body planet : planets) {
			applyGravity(sun, planet);
		}
		// for (final Body planet : planets) {
		// applyGravity(planet, sun);
		// }

		int totalScore = 0;
		if (player != null) {
			// player-planet forces
			for (int i = 0; i < planets.size; i++) {
				int score = 0;
				score += applyGravity(planets.get(i), player);
				totalScore += score;
				addScore(i, score);
			}
			// player-sun force
			player.applyForceToCenter(player.getWorldCenter().cpy().nor().scl(-1 / player.getWorldCenter().len2()), true);
		}
		return totalScore;
	}

	private void addScore(int i, int addScore) {
		final Integer totalScore = this.planetScores.get(i) + addScore;
		this.planetScores.set(i, MathUtils.clamp(totalScore, 0, 1000));
	}

	private int applyGravity(Body attractor, Body attracted) {
		final Vector2 diffVec = attractor.getWorldCenter().cpy().sub(attracted.getWorldCenter());
		final float dst2 = diffVec.len2();
		float gravity = gravConst * (attracted.getMass() * attracted.getMass()) / dst2;
		if (!MathUtils.isEqual(attractor.getMass(), 0f)) {
			gravity = gravConst * (attractor.getMass() * attracted.getMass()) / dst2;
		}
		attracted.applyForceToCenter(diffVec.nor().scl(gravity), true);

		int score = 0;
		final float minScoreDist = 50f;
		if (dst2 < minScoreDist) {
			Gdx.app.log(TAG, "dst2 " + dst2);
			score = Math.round((minScoreDist * 10f) / dst2);
		}
		return score;
	}

	public Color getSunColor() {
		return sunColor;
	}

	public void setSunColor(Color sunColor) {
		this.sunColor = sunColor;
	}

	public float getGravConst() {
		return gravConst;
	}

	public void setGravConst(float gravConst) {
		this.gravConst = gravConst;
	}

	public Array<Body> getPlanets() {
		return planets;
	}

	public void setPlanets(Array<Body> planets) {
		this.planets = planets;
	}

	public Body getSun() {
		return sun;
	}

	public void setSun(Body sun) {
		this.sun = sun;
	}

	public Array<Integer> getPlanetScores() {
		return planetScores;
	}

	public void setPlanetScores(Array<Integer> planetScores) {
		this.planetScores = planetScores;
	}

	public String getRank() {
		final float planetCount = planetScores.size;
		float planetsWithScore = 0;
		for (final Integer score : planetScores) {
			if (score > 0) {
				planetsWithScore += 1;
			}
		}
		if (planetsWithScore / planetCount > 0.99 && getScore() == planetCount * 1000) {
			return "S";
		} else if (planetsWithScore / planetCount > 0.9) {
			return "A";
		} else if (planetsWithScore / planetCount > 0.75) {
			return "B";
		} else if (planetsWithScore / planetCount > 0.6) {
			return "C";
		} else if (planetsWithScore / planetCount > 0.49) {
			return "D";
		} else if (planetsWithScore / planetCount > 0.3) {
			return "E";
		}
		return "F";
	}
}
