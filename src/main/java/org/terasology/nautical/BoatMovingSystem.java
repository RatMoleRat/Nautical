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
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.events.MovedEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

@RegisterSystem
public class BoatMovingSystem extends BaseComponentSystem {

    private static  final Logger logger = LoggerFactory.getLogger(BoatMovingSystem.class);
    @In
    WorldProvider worldProvider;
    @In
    BlockManager blockManager;
    @In
    EntityManager entityManager;

    private Block boat;
    private Block water;
    private boolean isActive = false;

    public void postBegin() {
        boat = blockManager.getBlock("Nautical:Boat");
        water = blockManager.getBlock("Core:Water");
    }
    @ReceiveEvent
    public void onCharacterMoved(MovedEvent event, EntityRef character) {

        if (isActive) {
            //TODO: rotate boat based on player rotation
            Vector3f pos = event.getPosition();
            Vector3f prevPos = new Vector3f(event.getPosition());
            prevPos.sub(event.getDelta());
            Vector3f previousBlockPos = new Vector3f(Math.round(prevPos.x * 2) / 2 + .5f, Math.round(prevPos.y * 2) / 2 - .5f, Math.round(prevPos.z*2)/2 + .5f);
            Vector3f currentBlockPos = new Vector3f(Math.round(pos.x*2)/2 + .5f, Math.round(pos.y*2)/2 -.5f, Math.round(pos.z*2)/2+ .5f);
            Vector3f waterCheck = new Vector3f(currentBlockPos.x, currentBlockPos.y - 1, currentBlockPos.z);
            if (worldProvider.getBlock(waterCheck).equals(water) && worldProvider.getBlock(currentBlockPos).equals(blockManager.getBlock("engine:air"))) {
                logger.info("equals");
                Prefab boat = entityManager.getPrefabManager().getPrefab("Boat");
                if (boat != null) {
                    boat.getComponent(LocationComponent.class).setWorldPosition(pos);
                }
            }
        }
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_CRITICAL)
    public void onBlockActivated(ActivateEvent event, EntityRef character) {
        logger.info("event triggered");
        Vector3f loc = event.getTargetLocation();
            logger.info("not null");
            isActive = !isActive;
            if (isActive) {
                logger.info("active");
                //TODO: teleport player to boat
                //character.getComponent(LocationComponent.class).setWorldPosition(new Vector3f(loc.x, loc.y+1, loc.z));
            }
    }
}
