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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
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
    private final Block boat = blockManager.getBlock("Nautical:Boat");
    private final Block water = blockManager.getBlock("Core:Water");

    @ReceiveEvent
    public void onCharacterMoved(MovedEvent event, EntityRef character) {

        //TODO: check for boat component on player
        logger.info("position: " + event.getPosition());
        Vector3f pos = event.getPosition();
        Vector3f prevPos = new Vector3f(event.getPosition());
        prevPos.sub(event.getDelta());
        Vector3f previousBlockPos = new Vector3f(prevPos.x + .5f, prevPos.y - 2.5f, prevPos.z + .5f);
        logger.info("previousBlockPos: " + previousBlockPos);
        Vector3f currentBlockPos = new Vector3f(pos.x + .5f, pos.y - 1.5f, pos.z + .5f);
        logger.info("currentBlockPos: " + currentBlockPos);
        logger.info("block at current pos: " + worldProvider.getBlock(currentBlockPos));
        if (previousBlockPos.x!=currentBlockPos.x || previousBlockPos.z!=currentBlockPos.z) {
            //TODO: only render boat if underwater
            worldProvider.setBlock(new Vector3i(currentBlockPos), boat);
            worldProvider.setBlock(new Vector3i(previousBlockPos), water);
            logger.info("block set");
        }
    }
}
