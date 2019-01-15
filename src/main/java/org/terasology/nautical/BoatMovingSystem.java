/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.nautical;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.Iterator;

@RegisterSystem(value = RegisterMode.ALWAYS)
@Share(BoatMovingSystem.class)
public class BoatMovingSystem extends BaseComponentSystem {

    private static  final Logger logger = LoggerFactory.getLogger(BoatMovingSystem.class);
    @In
    WorldProvider worldProvider;
    @In
    BlockManager blockManager;
    @In
    EntityManager entityManager;

    private Block water;
   // private Vector3f prevPos = Vector3f.zero();
    private boolean isActive = false;

    private EntityRef boat;
    private LocationComponent boatLocationComp;
    private MeshComponent boatMeshComp;

    private float boatLocY;
    private float boatHeight;

    public void postBegin() {
        water = blockManager.getBlock("Core:Water");
    }
    @ReceiveEvent
    public void onCharacterMoved(MovedEvent event, EntityRef character) {

        if (isActive) {
            //TODO: rotate boat based on player rotation
            Vector3f pos = event.getPosition();
            LocationComponent charLoc = character.getComponent(LocationComponent.class);
            Vector3f charPos = charLoc.getWorldPosition();
            Vector3f currentBlockPos = new Vector3f(pos.x  + .5f, pos.y + .5f, pos.z + .5f);
            Vector3f waterCheck = new Vector3f(currentBlockPos.x, currentBlockPos.y - 1, currentBlockPos.z);
            if (worldProvider.getBlock(waterCheck).equals(water) && worldProvider.getBlock(currentBlockPos).equals(blockManager.getBlock("engine:air"))) {

                if (boat != null) {
                    logger.info("charPos: "+charPos+ "   boatPos: "+boatLocationComp.getWorldPosition());
                    boatMeshComp.mesh.getAABB().transform(new Quat4f(0, 0, 0, 0), boatLocationComp.getWorldPosition().sub(new Vector3f(charPos.x, 0, charPos.z)), 1);
                    boat.getComponent(LocationComponent.class).setWorldPosition(new Vector3f(charPos.x, boatLocY + boatHeight / 2, charPos.z));
                    //todo: TEST this, figure out why boat isn't moving
                }
            }
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onBlockActivated(ActivateEvent event, EntityRef character) {
        //logger.info("event triggered");
        Vector3f loc = event.getTargetLocation();
           // logger.info("not null");
            isActive = !isActive;
            if (isActive) {
                logger.info("active");
                //TODO: teleport player to boat
                //character.getComponent(LocationComponent.class).setWorldPosition(new Vector3f(loc.x, loc.y+1, loc.z));
            }
    }

    public void boat(EntityRef ref) {
        boat = ref;
        boatLocationComp = boat.getComponent(LocationComponent.class);
        boatMeshComp = boat.getComponent(MeshComponent.class);
        boatLocY = boatLocationComp.getWorldPosition().y;
        boatHeight = boatMeshComp.mesh.getAABB().maxY() - boatMeshComp.mesh.getAABB().minY() + .1f;
    }
}
