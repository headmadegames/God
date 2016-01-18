package headmade.god;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class SolarScreenInputProcessor implements InputProcessor {
	private static final String	TAG	= SolarScreenInputProcessor.class.getName();

	private SolarSystemScreen	screen;

	public SolarScreenInputProcessor(SolarSystemScreen solarSystemScreen) {
		this.screen = solarSystemScreen;
	}

	@Override
	public boolean keyDown(int keycode) {
		if (keycode == Keys.LEFT) {
			screen.cam.translate(-1f, 0f);
			screen.cam.update();
			return true;
		} else if (keycode == Keys.RIGHT) {
			screen.cam.translate(1f, 0f);
			screen.cam.update();
			return true;
		} else if (keycode == Keys.UP) {
			screen.cam.translate(0f, 1f);
			screen.cam.update();
			return true;
		} else if (keycode == Keys.DOWN) {
			screen.cam.translate(0f, -1f);
			screen.cam.update();
			return true;
		} else if (keycode == Keys.A) {
			screen.turnLeft = true;
			return true;
		} else if (keycode == Keys.D) {
			screen.turnRight = true;
			return true;
		} else if (keycode == Keys.W) {
			screen.accelerate = true;
			return true;
		} else if (keycode == Keys.S) {
			screen.deaccelerate = true;
			return true;
		} else if (keycode == Keys.F12) {
			screen.debugEnabled = !screen.debugEnabled;
			return true;
		} else if (keycode == Keys.SPACE) {
			if (screen.showScore) {
				screen.shouldReset = true;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (keycode == Keys.A) {
			screen.turnLeft = false;
			return true;
		} else if (keycode == Keys.D) {
			screen.turnRight = false;
			return true;
		} else if (keycode == Keys.W) {
			screen.accelerate = false;
			return true;
		} else if (keycode == Keys.S) {
			screen.deaccelerate = false;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		final Vector3 mouse = screen.cam.unproject(new Vector3(screenX, screenY, 0));
		Gdx.app.log(TAG, "Mouse clicked at " + mouse);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		screen.cam.zoom += amount * 0.5f;
		screen.cam.zoom = MathUtils.clamp(screen.cam.zoom, 0.5f, 50f);
		screen.cam.update();
		Gdx.app.log(TAG, "new zoom " + screen.cam.zoom);
		return false;
	}

}
