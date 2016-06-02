/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.taskmanager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.network.serverpackets.DeleteObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author la2 Lets drink to code!
 */
public class DecayTaskManager
{
	protected static final Logger _log = LoggerFactory.getLogger(DecayTaskManager.class);
	
	protected final Map<L2Character, Long> _decayTasks = new ConcurrentHashMap<>();
	
	public static Set<Integer> _decayed = ConcurrentHashMap.newKeySet();
	
	protected DecayTaskManager()
	{
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new DecayScheduler(), 10000, Config.DECAY_TIME_TASK);
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> _decayed.clear(), 60 * 1000, 60 * 1000);
	}
	
	public static DecayTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void addDecayTask(L2Character actor)
	{
		addDecayTask(actor, 0);
	}
	
	public void addDecayTask(L2Character actor, int interval)
	{
		_decayTasks.put(actor, System.currentTimeMillis() + interval);
	}
	
	public void cancelDecayTask(L2Character actor)
	{
		_decayTasks.remove(actor);
	}
	
	protected class DecayScheduler implements Runnable
	{
		@Override
		public void run()
		{
			final long current = System.currentTimeMillis();
			L2Character actor;
			Long next;
			int delay;
			
			for (Map.Entry<L2Character, Long> entry : _decayTasks.entrySet())
			{
				actor = entry.getKey();
				next = entry.getValue();
				
				if (actor.isRaid() && !actor.isRaidMinion())
				{
					delay = Config.RAID_BOSS_DECAY_TIME;
				}
				else if ((actor instanceof L2Attackable) && (((L2Attackable) actor).isSpoiled() || ((L2Attackable) actor).isSeeded()))
				{
					delay = Config.SPOILED_DECAY_TIME;
				}
				else
				{
					delay = Config.NPC_DECAY_TIME;
				}
				
				if ((current - next) > delay)
				{
					actor.onDecay();
					_decayTasks.remove(actor);
					
					_decayed.add(actor.getObjectId());
					
					// vGodFather TODO find better way or not?
					L2Character object = actor;
					L2World.getInstance().getPlayers().stream().filter(pc -> pc.isOnline() && !pc.isInOfflineMode()).forEach(pc -> pc.sendPacket(new DeleteObject(object)));
				}
			}
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder ret = new StringBuilder();
		ret.append("============= DecayTask Manager Report ============");
		ret.append(Config.EOL);
		ret.append("Tasks count: ");
		ret.append(_decayTasks.size());
		ret.append(Config.EOL);
		ret.append("Tasks dump:");
		ret.append(Config.EOL);
		
		Long current = System.currentTimeMillis();
		for (L2Character actor : _decayTasks.keySet())
		{
			ret.append("Class/Name: ");
			ret.append(actor.getClass().getSimpleName());
			ret.append('/');
			ret.append(actor.getName());
			ret.append(" decay timer: ");
			ret.append(current - _decayTasks.get(actor));
			ret.append(Config.EOL);
		}
		
		return ret.toString();
	}
	
	/**
	 * <u><b><font color="FF0000">Read only</font></b></u>
	 * @return
	 */
	public Map<L2Character, Long> getTasks()
	{
		return _decayTasks;
	}
	
	private static class SingletonHolder
	{
		protected static final DecayTaskManager _instance = new DecayTaskManager();
	}
}
