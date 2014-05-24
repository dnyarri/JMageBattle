/*    This file is part of Arkhados.

 Arkhados is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Arkhados is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Arkhados.  If not, see <http://www.gnu.org/licenses/>. */

package arkhados.spell.spells.elitesoldier;

import arkhados.WorldManager;
import arkhados.actions.EntityAction;
import arkhados.actions.SplashAction;
import arkhados.actions.castspellactions.CastProjectileAction;
import arkhados.controls.EntityEventControl;
import arkhados.controls.ProjectileControl;
import arkhados.controls.SpellBuffControl;
import arkhados.controls.TimedExistenceControl;
import arkhados.entityevents.RemovalEventAction;
import arkhados.spell.CastSpellActionBuilder;
import arkhados.spell.Spell;
import arkhados.util.DistanceScaling;
import arkhados.util.NodeBuilder;
import arkhados.util.UserDataStrings;
import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.collision.shapes.SphereCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Sphere;

/**
 * EliteSoldiers's RocketLauncher (E) spell. Projectile has moderate speed and deals
 * high damage and small splash damage. Has big knockback effect on hit.
 */
public class RocketLauncher extends Spell {
    {
        this.iconName = "rocket_launcher.png";
        this.setMoveTowardsTarget(false);
    }

    public RocketLauncher(String name, float cooldown, float range, float castTime) {
        super(name, cooldown, range, castTime);
    }

    public static Spell create() {
        final float cooldown = 4f;
        final float range = 80f;
        final float castTime = 0.3f;

        final RocketLauncher spell = new RocketLauncher("Rocket Launcher", cooldown, range, castTime);

        spell.castSpellActionBuilder = new CastSpellActionBuilder() {            
            public EntityAction newAction(Node caster, Vector3f location) {
                final CastProjectileAction castProjectile = new CastProjectileAction(spell, Spell.worldManager);
                return castProjectile;
            }
        };

        spell.nodeBuilder = new RocketBuilder();

        return spell;
    }
}

class RocketBuilder extends NodeBuilder {

    private ParticleEmitter createSmokeEmitter() {
        final ParticleEmitter smoke = new ParticleEmitter("smoke-emitter", ParticleMesh.Type.Triangle, 300);
        Material materialGray = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialGray.setTexture("Texture", NodeBuilder.assetManager.loadTexture("Effects/flame.png"));
        smoke.setMaterial(materialGray);
        smoke.setImagesX(2);
        smoke.setImagesY(2);
        smoke.setSelectRandomImage(true);
        smoke.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        smoke.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f));
        smoke.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
//        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Z.mult(10));
//        fire.getParticleInfluencer().setVelocityVariation(0.5f);
        smoke.setStartSize(2.0f);
        smoke.setEndSize(6.0f);
        smoke.setGravity(Vector3f.ZERO);
        smoke.setLowLife(1f);
        smoke.setHighLife(1.3f);
        smoke.setParticlesPerSec(100);

        smoke.setRandomAngle(true);
        return smoke;
    }

    private ParticleEmitter createFireEmitter() {
        final ParticleEmitter fire = new ParticleEmitter("fire-emitter", ParticleMesh.Type.Triangle, 200);
        Material materialRed = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialRed.setTexture("Texture", NodeBuilder.assetManager.loadTexture("Effects/flame.png"));
        fire.setMaterial(materialRed);
        fire.setImagesX(2);
        fire.setImagesY(2);
        fire.setSelectRandomImage(true);
        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 1.0f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.5f));
        fire.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO);
//        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_Z.mult(10));
//        fire.getParticleInfluencer().setVelocityVariation(0.5f);
        fire.setStartSize(7.5f);
        fire.setEndSize(1.5f);
        fire.setGravity(Vector3f.ZERO);
        fire.setLowLife(0.1f);
        fire.setHighLife(0.1f);
        fire.setParticlesPerSec(100);

        fire.setRandomAngle(true);
        return fire;
    }

    @Override
    public Node build() {
        final Sphere sphere = new Sphere(32, 32, 1);

        final Geometry projectileGeom = new Geometry("projectile-geom", sphere);
        projectileGeom.setCullHint(Spatial.CullHint.Always);

        final Node node = new Node("projectile");
        node.attachChild(projectileGeom);

        // TODO: Give at least bit better material
        final Material material = new Material(NodeBuilder.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        material.setColor("Color", ColorRGBA.Yellow);
        node.setMaterial(material);

        node.setUserData(UserDataStrings.SPEED_MOVEMENT, 140f);
        node.setUserData(UserDataStrings.MASS, 0.30f);
        node.setUserData(UserDataStrings.DAMAGE, 150f);
        node.setUserData(UserDataStrings.IMPULSE_FACTOR, 23000f);

        if (NodeBuilder.worldManager.isClient()) {
            final ParticleEmitter fire = this.createFireEmitter();
            node.attachChild(fire);

            final ParticleEmitter smoke = this.createSmokeEmitter();
            node.attachChild(smoke);

            node.addControl(new EntityEventControl());
            /**
             * Here we specify what happens on client side when fireball is
             * removed. In this case we want explosion effect.
             */
            final RocketRemovalAction removalAction = new RocketRemovalAction(assetManager);
            removalAction.setFireEmitter(fire);
            removalAction.setSmokeTrail(smoke);


            node.getControl(EntityEventControl.class).setOnRemoval(removalAction);
        }

        final SphereCollisionShape collisionShape = new SphereCollisionShape(10.0f);
        final RigidBodyControl physicsBody = new RigidBodyControl(collisionShape, (Float) node.getUserData(UserDataStrings.MASS));
        /**
         * We don't want projectiles to collide with each other so we give them
         * their own collision group and prevent them from colliding with that
         * group.
         */
        physicsBody.setCollisionGroup(RigidBodyControl.COLLISION_GROUP_16);
        physicsBody.removeCollideWithGroup(RigidBodyControl.COLLISION_GROUP_16);

        /**
         * Add collision group of characters
         */
        physicsBody.addCollideWithGroup(RigidBodyControl.COLLISION_GROUP_02);

        node.addControl(physicsBody);

        ProjectileControl projectileControl = new ProjectileControl();
        SplashAction splash = new SplashAction(100f, 30f, DistanceScaling.CONSTANT, null);
        splash.setSpatial(node);
        projectileControl.setSplashAction(splash);
        node.addControl(projectileControl);
        final SpellBuffControl buffControl = new SpellBuffControl();
        node.addControl(buffControl);
        return node;
    }
}

class RocketRemovalAction implements RemovalEventAction {

    private ParticleEmitter fire;
    private ParticleEmitter smokeTrail;
    private AssetManager assetManager;
    private AudioNode sound;

    public RocketRemovalAction(AssetManager assetManager) {
        this.assetManager = assetManager;
        this.sound = new AudioNode(assetManager, "Effects/Sound/FireballExplosion.wav");
        this.sound.setPositional(true);
        this.sound.setReverbEnabled(false);
        this.sound.setVolume(1f);
    }

    public void setFireEmitter(ParticleEmitter fire) {
        this.fire = fire;
    }

    private void leaveSmokeTrail(final Node worldRoot, Vector3f worldTranslation) {
        this.smokeTrail.setParticlesPerSec(0);
        worldRoot.attachChild(this.smokeTrail);
        this.smokeTrail.setLocalTranslation(worldTranslation);
        this.smokeTrail.addControl(new TimedExistenceControl(5f));
    }

    private void createSmokePuff(final Node worldRoot, Vector3f worldTranslation) {
        final ParticleEmitter smokePuff = new ParticleEmitter("smoke-puff", ParticleMesh.Type.Triangle, 20);
        Material materialGray = new Material(this.assetManager, "Common/MatDefs/Misc/Particle.j3md");
        materialGray.setTexture("Texture", this.assetManager.loadTexture("Effects/flame.png"));
        smokePuff.setMaterial(materialGray);
        smokePuff.setImagesX(2);
        smokePuff.setImagesY(2);
        smokePuff.setSelectRandomImage(true);
        smokePuff.setStartColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.2f));
        smokePuff.setEndColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 0.1f));

        smokePuff.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_X.mult(5.0f));
        smokePuff.getParticleInfluencer().setVelocityVariation(1f);

        smokePuff.setStartSize(8.0f);
        smokePuff.setEndSize(30.0f);
        smokePuff.setGravity(Vector3f.ZERO);
        smokePuff.setLowLife(1.75f);
        smokePuff.setHighLife(6f);
        smokePuff.setParticlesPerSec(0);

        smokePuff.setRandomAngle(true);

        smokePuff.setShape(new EmitterSphereShape(Vector3f.ZERO, 4.0f));
        worldRoot.attachChild(smokePuff);
        smokePuff.setLocalTranslation(worldTranslation);
        smokePuff.emitAllParticles();
    }

    public void exec(WorldManager worldManager, String reason) {
        Node node = new Node("rocket-explosion");
        Vector3f worldTranslation = fire.getParent().getLocalTranslation();
        this.leaveSmokeTrail(node, worldTranslation);
        this.createSmokePuff(node, worldTranslation);

        fire.removeFromParent();
        node.attachChild(fire);
        node.attachChild(this.sound);
        node.setLocalTranslation(worldTranslation);
        fire.setLocalTranslation(Vector3f.ZERO);
        worldManager.getWorldRoot().attachChild(node);
        node.addControl(new TimedExistenceControl(1f));

        fire.setStartColor(new ColorRGBA(0.95f, 0.150f, 0.0f, 0.40f));
        fire.setEndColor(new ColorRGBA(1.0f, 1.0f, 0.0f, 0.0f));
        fire.setLowLife(0.4f);
        fire.setHighLife(0.5f);
        fire.setNumParticles(120);
        fire.setStartSize(7.50f);
        fire.setEndSize(25f);
        fire.getParticleInfluencer().setInitialVelocity(Vector3f.UNIT_X.mult(45.0f));
        fire.getParticleInfluencer().setVelocityVariation(1f);

//        fire.setShape(new EmitterSphereShape(Vector3f.ZERO, 2.0f));
        fire.emitAllParticles();
        fire.setParticlesPerSec(0.0f);

//        this.sound.setLocalTranslation(worldTranslation);        
        this.sound.setVolume(5f);
        this.sound.play();
    }

    public void setSmokeTrail(ParticleEmitter smoke) {
        this.smokeTrail = smoke;
    }
}
