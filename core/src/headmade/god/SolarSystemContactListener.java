package headmade.god;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

public class SolarSystemContactListener implements ContactListener {
	private static final String	TAG	= SolarSystemContactListener.class.getName();

	private SolarSystemScreen	solarSystemScreen;

	public SolarSystemContactListener(SolarSystemScreen solarSystemScreen) {
		this.solarSystemScreen = solarSystemScreen;
	}

	@Override
	public void beginContact(Contact contact) {
		Gdx.app.log(TAG, "CONTACT! Reset!");
		solarSystemScreen.showScore = true;
		solarSystemScreen.shouldDestroyPlayer = true;
		Assets.instance.playSound(Assets.sndExplosion);
	}

	@Override
	public void endContact(Contact contact) {
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}

}
