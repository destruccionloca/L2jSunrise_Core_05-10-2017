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
package l2r.gameserver.communitybbs.Managers;

import java.io.File;
import java.util.List;
import java.util.StringTokenizer;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.cache.HtmCache;
import l2r.gameserver.data.sql.CharNameTable;
import l2r.gameserver.data.sql.ClanTable;
import l2r.gameserver.data.xml.impl.HennaData;
import l2r.gameserver.data.xml.impl.ItemData;
import l2r.gameserver.data.xml.impl.MultisellData;
import l2r.gameserver.enums.CtrlIntention;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.instancemanager.SiegeManager;
import l2r.gameserver.instancemanager.TownManager;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.entity.olympiad.OlympiadManager;
import l2r.gameserver.model.items.L2Henna;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExBuySellList;
import l2r.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import l2r.gameserver.network.serverpackets.ExShowVariationMakeWindow;
import l2r.gameserver.network.serverpackets.HennaEquipList;
import l2r.gameserver.network.serverpackets.HennaRemoveList;
import l2r.gameserver.network.serverpackets.MagicSkillUse;
import l2r.gameserver.network.serverpackets.PartySmallWindowAll;
import l2r.gameserver.network.serverpackets.PartySmallWindowDeleteAll;
import l2r.gameserver.network.serverpackets.SetupGauge;
import l2r.gameserver.network.serverpackets.ShowBoard;
import l2r.gameserver.network.serverpackets.WareHouseDepositList;
import l2r.gameserver.util.Broadcast;
import l2r.gameserver.util.Util;
import gr.sr.configsEngine.configs.impl.CommunityServicesConfigs;
import gr.sr.datatables.SunriseTable;
import gr.sr.interf.SunriseEvents;
import gr.sr.javaBuffer.AutoBuff;
import gr.sr.javaBuffer.BufferPacketCategories;
import gr.sr.javaBuffer.BufferPacketSender;
import gr.sr.javaBuffer.JavaBufferBypass;
import gr.sr.javaBuffer.PlayerMethods;
import gr.sr.javaBuffer.buffCommunity.dynamicHtmls.GenerateHtmls;
import gr.sr.javaBuffer.runnable.BuffDeleter;
import gr.sr.main.Conditions;
import gr.sr.securityEngine.SecurityActions;
import gr.sr.securityEngine.SecurityType;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public class ServicesBBSManager extends BaseBBSManager
{
	public String _servicesBBSCommand = CommunityServicesConfigs.BYPASS_COMMAND;
	
	@Override
	public void cbByPass(String command, L2PcInstance activeChar)
	{
		if (!CommunityServicesConfigs.COMMUNITY_SERVICES_ALLOW)
		{
			activeChar.sendMessage("This function is disabled by admin.");
			return;
		}
		
		String path = "data/html/CommunityBoard/services/";
		String filepath = "";
		String content = "";
		
		if (command.equals(_servicesBBSCommand + ""))
		{
			filepath = path + "main.htm";
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filepath);
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + ";gatekeeper"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			String file = st.nextToken();
			path = "data/html/CommunityBoard/services/gatekeeper/";
			sendHtm(activeChar, filepath, path, file, command);
		}
		else if (command.startsWith(_servicesBBSCommand + ";"))
		{
			StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			String file = st.nextToken();
			sendHtm(activeChar, filepath, path, file, command);
		}
		else if (command.startsWith(_servicesBBSCommand + "_sendMultisell"))
		{
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_SHOP_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin");
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_SHOP_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				try
				{
					String multisell = commandSeperator(command);
					int multi = Integer.valueOf(multisell);
					activeChar.setIsUsingAioMultisell(true);
					MultisellData.getInstance().separateAndSend(multi, activeChar, null, false);
					
					if ((multi == 539) || (multi == 540) || (multi == 541))
					{
						content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop.htm");
					}
					else if ((multi == 527) || (multi == 528) || (multi == 529) || (multi == 530) || (multi == 531) || (multi == 532) || (multi == 533) || (multi == 534) || (multi == 535) || (multi == 536) || (multi == 537) || (multi == 538))
					{
						content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/blacksmith.htm");
					}
					else if (multi == 525)
					{
						content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/symbolMaker.htm");
					}
					else if (multi == 526)
					{
						content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
					}
					else
					{
						content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/gmshop.htm");
					}
					
					separateAndSend(content, activeChar);
				}
				catch (Exception e)
				{
					SecurityActions.startSecurity(activeChar, SecurityType.COMMUNITY_SYSTEM);
				}
			}
		}
		else if (command.startsWith(_servicesBBSCommand + "_CommunitySell"))
		{
			activeChar.sendPacket(new ExBuySellList(activeChar, 0, true));
		}
		else if (command.startsWith(_servicesBBSCommand + "_teleport"))
		{
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/gatekeeper/main_gk.htm");
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_TP_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin");
				separateAndSend(content, activeChar);
				return;
			}
			
			if (activeChar.isJailed() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || SunriseEvents.isInEvent(activeChar) || OlympiadManager.getInstance().isRegistered(activeChar))
			{
				activeChar.sendMessage("Cannot use at the moment.");
				separateAndSend(content, activeChar);
				return;
			}
			
			try
			{
				String tp = commandSeperator(command);
				Integer[] c = new Integer[3];
				c[0] = SunriseTable.getInstance().getTeleportInfo(Integer.parseInt(tp))[0];
				c[1] = SunriseTable.getInstance().getTeleportInfo(Integer.parseInt(tp))[1];
				c[2] = SunriseTable.getInstance().getTeleportInfo(Integer.parseInt(tp))[2];
				boolean onlyForNobless = SunriseTable.getInstance().getTeleportInfo(Integer.parseInt(tp))[3] == 1;
				int itemIdToGet = SunriseTable.getInstance().getTeleportInfo(Integer.parseInt(tp))[4];
				int price = SunriseTable.getInstance().getTeleportInfo(Integer.parseInt(tp))[5];
				
				if (!CommunityServicesConfigs.ALLOW_TELEPORT_DURING_SIEGE)
				{
					if (SiegeManager.getInstance().getSiege(c[0], c[1], c[2]) != null)
					{
						activeChar.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
						separateAndSend(content, activeChar);
						return;
					}
					else if (TownManager.townHasCastleInSiege(c[0], c[1]) && activeChar.isInsideZone(ZoneIdType.TOWN))
					{
						activeChar.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
						separateAndSend(content, activeChar);
						return;
					}
				}
				
				if (Conditions.checkPlayerItemCount(activeChar, itemIdToGet, price))
				{
					if (onlyForNobless && !activeChar.isNoble() && !activeChar.isGM())
					{
						activeChar.sendMessage("Only noble chars can teleport there.");
						separateAndSend(content, activeChar);
						return;
					}
					
					if (activeChar.isTransformed())
					{
						if ((activeChar.getTransformationId() == 9) || (activeChar.getTransformationId() == 8))
						{
							activeChar.untransform();
						}
					}
					
					if (activeChar.isInsideZone(ZoneIdType.PEACE) || activeChar.isGM())
					{
						activeChar.teleToLocation(c[0], c[1], c[2]);
					}
					else
					{
						activeChar.abortCast();
						activeChar.abortAttack();
						activeChar.sendPacket(ActionFailed.STATIC_PACKET);
						activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						activeChar.setTarget(activeChar);
						activeChar.disableAllSkills();
						Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, 1050, 1, 30000, 0), 810000);
						activeChar.sendPacket(new SetupGauge(0, 30000));
						activeChar.setSkillCast(ThreadPoolManager.getInstance().scheduleGeneral(new Teleport(activeChar, c[0], c[1], c[2]), 30000));
						activeChar.forceIsCasting(10 + GameTimeController.getInstance().getGameTicks() + (30000 / GameTimeController.MILLIS_IN_TICK));
					}
					
					activeChar.destroyItemByItemId("Community Teleport", itemIdToGet, price, activeChar, true);
				}
			}
			catch (Exception e)
			{
				SecurityActions.startSecurity(activeChar, SecurityType.COMMUNITY_SYSTEM);
			}
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_drawSymbol"))
		{
			List<L2Henna> tato = HennaData.getInstance().getHennaList(activeChar.getClassId());
			activeChar.sendPacket(new HennaEquipList(activeChar, tato));
			
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/symbolMaker.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_removeSymbol"))
		{
			boolean hasHennas = false;
			for (int i = 1; i <= 3; i++)
			{
				L2Henna henna = activeChar.getHenna(i);
				if (henna != null)
				{
					hasHennas = true;
				}
			}
			
			if (hasHennas)
			{
				activeChar.sendPacket(new HennaRemoveList(activeChar));
			}
			else
			{
				activeChar.sendMessage("You do not have dyes.");
			}
			
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/symbolMaker.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_addAugment"))
		{
			activeChar.sendPacket(new ExShowVariationMakeWindow());
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/blacksmith.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_delAugment"))
		{
			activeChar.sendPacket(new ExShowVariationCancelWindow());
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/blacksmith.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.toLowerCase().startsWith(_servicesBBSCommand + "_pwithdraw"))
		{
			GenerateHtmls.showPWithdrawWindow(activeChar, null, (byte) 0);
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.toLowerCase().startsWith(_servicesBBSCommand + "_cwithdraw"))
		{
			GenerateHtmls.showCWithdrawWindow(activeChar, null, (byte) 0);
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_ndeposit"))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.setActiveWarehouse(activeChar.getWarehouse());
			if (activeChar.getWarehouse().getSize() == activeChar.getWareHouseLimit())
			{
				activeChar.sendPacket(SystemMessageId.WAREHOUSE_FULL);
				return;
			}
			activeChar.setIsUsingAioWh(true);
			activeChar.tempInventoryDisable();
			activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.PRIVATE));
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_clandeposit"))
		{
			if (activeChar.getClan() == null)
			{
				activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
				return;
			}
			
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
			if (activeChar.getClan().getLevel() == 0)
			{
				activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
				return;
			}
			
			activeChar.setIsUsingAioWh(true);
			activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
			activeChar.tempInventoryDisable();
			activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.CLAN));
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/warehouse.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_washPK"))
		{
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop_decreasePK.htm");
			if (activeChar.getPkKills() > 0)
			{
				content = content.replaceAll("%replace%", buttons(activeChar));
			}
			else
			{
				content = content.replaceAll("%replace%", "<table width=750 height=20><tr><td align=center>You dont have PK Points to wash.</td></tr></table>");
			}
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_deletePK"))
		{
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop_decreasePK.htm");
			content = content.replaceAll("%replace%", buttons(activeChar));
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin");
				separateAndSend(content, activeChar);
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				int i = Integer.parseInt(commandSeperator(command));
				if (Conditions.checkPlayerItemCount(activeChar, CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_PRICE * i))
				{
					int kills = activeChar.getPkKills();
					if ((kills - i) >= 0)
					{
						activeChar.setPkKills(kills - i);
						activeChar.broadcastUserInfo();
						activeChar.sendMessage(i + " PK points removed.");
						activeChar.destroyItemByItemId("Community Decrease PK", CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_PRICE * i, activeChar, true);
					}
				}
			}
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_changename"))
		{
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop.htm");
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin");
				separateAndSend(content, activeChar);
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				try
				{
					String val = commandSeperator(command);
					if (!Util.isAlphaNumeric(val))
					{
						activeChar.sendMessage("Invalid character name.");
						separateAndSend(content, activeChar);
						return;
					}
					
					if (Conditions.checkPlayerItemCount(activeChar, CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_PRICE))
					{
						if (CharNameTable.getInstance().getIdByName(val) > 0)
						{
							activeChar.sendMessage("Warning, name " + val + " already exists.");
							separateAndSend(content, activeChar);
							return;
						}
						
						activeChar.destroyItemByItemId("Community Name Change", CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_NAME_CHANGE_PRICE, activeChar, true);
						activeChar.setName(val);
						activeChar.getAppearance().setVisibleName(val);
						activeChar.store();
						activeChar.sendMessage("Your name has been changed to " + val);
						activeChar.broadcastUserInfo();
						
						if (activeChar.isInParty())
						{
							// Delete party window for other party members
							activeChar.getParty().broadcastToPartyMembers(activeChar, new PartySmallWindowDeleteAll());
							for (L2PcInstance member : activeChar.getParty().getMembers())
							{
								// And re-add
								if (member != activeChar)
								{
									member.sendPacket(new PartySmallWindowAll(member, activeChar.getParty()));
								}
							}
						}
						
						if (activeChar.getClan() != null)
						{
							activeChar.getClan().broadcastClanStatus();
						}
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Player name box cannot be empty.");
					separateAndSend(content, activeChar);
				}
			}
			separateAndSend(content, activeChar);
		}
		// Change clan name
		else if (command.startsWith(_servicesBBSCommand + "_changeclanname"))
		{
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/exclusiveShop.htm");
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_ALLOW)
			{
				activeChar.sendMessage("This function is disabled by admin");
				separateAndSend(content, activeChar);
				return;
			}
			
			if (!CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_NONPEACE && !activeChar.isInsideZone(ZoneIdType.PEACE))
			{
				activeChar.sendMessage("You cannot use this function outside peace zone.");
			}
			else
			{
				try
				{
					String val = commandSeperator(command);
					if ((activeChar.getClan() == null) || !activeChar.isClanLeader())
					{
						activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
						separateAndSend(content, activeChar);
						return;
					}
					
					if (!Util.isAlphaNumeric(val))
					{
						activeChar.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
						separateAndSend(content, activeChar);
						return;
					}
					
					if (Conditions.checkPlayerItemCount(activeChar, CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_PRICE))
					{
						if (ClanTable.getInstance().getClanByName(val) != null)
						{
							activeChar.sendMessage("Warning, clan name " + val + " already exists.");
							separateAndSend(content, activeChar);
							return;
						}
						
						activeChar.destroyItemByItemId("Community Clan Name Change", CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_ID, CommunityServicesConfigs.COMMUNITY_SERVICES_CLAN_NAME_CHANGE_PRICE, activeChar, true);
						activeChar.getClan().setName(val);
						activeChar.getClan().updateClanNameInDB();
						activeChar.sendMessage("Your clan name has been changed to " + val);
						activeChar.broadcastUserInfo();
						
						if (activeChar.isInParty())
						{
							// Delete party window for other party members
							activeChar.getParty().broadcastToPartyMembers(activeChar, new PartySmallWindowDeleteAll());
							for (L2PcInstance member : activeChar.getParty().getMembers())
							{
								// And re-add
								if (member != activeChar)
								{
									member.sendPacket(new PartySmallWindowAll(member, activeChar.getParty()));
								}
							}
						}
						
						if (activeChar.getClan() != null)
						{
							activeChar.getClan().broadcastClanStatus();
						}
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
					activeChar.sendMessage("Clan name box cannot be empty.");
					separateAndSend(content, activeChar);
				}
			}
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_buffer"))
		{
			content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/buffer/main.htm");
			separateAndSend(content, activeChar);
		}
		else if (command.startsWith(_servicesBBSCommand + "_functions_buffer"))
		{
			final String[] subCommand = command.split("_");
			
			if (((activeChar.isInCombat() || (activeChar.getPvpFlag() != 0)) && !activeChar.isInsideZone(ZoneIdType.PEACE)) || activeChar.isJailed() || activeChar.isAlikeDead() || activeChar.isInOlympiadMode() || activeChar.inObserverMode() || SunriseEvents.isInEvent(activeChar) || OlympiadManager.getInstance().isRegistered(activeChar))
			{
				activeChar.sendMessage("Cannot use at the moment.");
				content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/buffer/main.htm");
				separateAndSend(content, activeChar);
				return;
			}
			
			// Page navigation, html command how to starts
			if (subCommand[4].startsWith("page"))
			{
				if (subCommand[5].isEmpty() || (subCommand[5] == null))
				{
					return;
				}
				
				content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/CommunityBoard/services/buffer/" + subCommand[5]);
				separateAndSend(content, activeChar);
			}
			// Method to remove all players buffs
			else if (subCommand[4].startsWith("removebuff"))
			{
				activeChar.stopAllEffects();
				BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Method to restore HP/MP/CP
			else if (subCommand[4].startsWith("healme"))
			{
				if ((activeChar.getPvpFlag() != 0) && !activeChar.isInsideZone(ZoneIdType.PEACE))
				{
					activeChar.sendMessage("Cannot use this feature here with flag.");
					BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
					return;
				}
				
				activeChar.setCurrentHpMp(activeChar.getMaxHp(), activeChar.getMaxMp());
				activeChar.setCurrentCp(activeChar.getMaxCp());
				BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Method to give auto buffs depends on class
			else if (subCommand[4].startsWith("autobuff"))
			{
				if ((activeChar.getPvpFlag() != 0) && !activeChar.isInsideZone(ZoneIdType.PEACE))
				{
					activeChar.sendMessage("Cannot use this feature here with flag.");
					BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
					return;
				}
				
				AutoBuff.autoBuff(activeChar);
				BufferPacketSender.sendPacket(activeChar, "functions.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Send buffs from profile to player or party or pet
			else if (subCommand[4].startsWith("bufffor"))
			{
				if (subCommand[4].startsWith("buffforpet"))
				{
					JavaBufferBypass.callPetBuffCommand(activeChar, subCommand[5]);
				}
				else if (subCommand[4].startsWith("buffforparty"))
				{
					JavaBufferBypass.callPartyBuffCommand(activeChar, subCommand[5]);
				}
				else if (subCommand[4].startsWith("buffforme"))
				{
					JavaBufferBypass.callSelfBuffCommand(activeChar, subCommand[5]);
				}
				
				BufferPacketSender.sendPacket(activeChar, "main.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			// Method to give single buffs
			else if (subCommand[4].startsWith("buff"))
			{
				JavaBufferBypass.callBuffCommand(activeChar, subCommand[5], subCommand[4], 1);
			}
			// Scheme create new profile
			else if (subCommand[4].startsWith("saveProfile"))
			{
				try
				{
					JavaBufferBypass.callSaveProfile(activeChar, subCommand[5], 1);
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Please specify a valid profile name.");
					BufferPacketSender.sendPacket(activeChar, "newSchemeProfile.htm", BufferPacketCategories.COMMUNITY, 1);
					return;
				}
			}
			else if (subCommand[4].startsWith("showAvaliable"))
			{
				JavaBufferBypass.callAvailableCommand(activeChar, subCommand[4], subCommand[5], 1);
			}
			else if (subCommand[4].startsWith("add"))
			{
				JavaBufferBypass.callAddCommand(activeChar, subCommand[4], subCommand[5], subCommand[6], 1);
			}
			// Method to delete player's selected profile
			else if (subCommand[4].startsWith("deleteProfile"))
			{
				PlayerMethods.delProfile(subCommand[5], activeChar);
				BufferPacketSender.sendPacket(activeChar, "main.htm", BufferPacketCategories.COMMUNITY, 1);
			}
			else if (subCommand[4].startsWith("showBuffsToDelete"))
			{
				GenerateHtmls.showBuffsToDelete(activeChar, subCommand[5], "removeBuffs");
			}
			else if (subCommand[4].startsWith("removeBuffs"))
			{
				ThreadPoolManager.getInstance().executeGeneral(new BuffDeleter(activeChar, subCommand[5], Integer.parseInt(subCommand[6]), 1));
			}
			else if (subCommand[4].startsWith("showProfiles"))
			{
				GenerateHtmls.showSchemeToEdit(activeChar, subCommand[5]);
			}
		}
		else
		{
			separateAndSend("<html><body><br><br><center>Command : " + command + " needs core development</center><br><br></body></html>", activeChar);
		}
	}
	
	private String buttons(L2PcInstance activeChar)
	{
		String add = "";
		final int[] PKS =
		{
			1,
			2,
			5,
			10,
			25,
			50,
			100,
			250,
			500,
			1000
		};
		
		for (int pk : PKS)
		{
			if (activeChar.getPkKills() <= pk)
			{
				break;
			}
			add += getPkButton(pk);
		}
		
		if (activeChar.getPkKills() != 0)
		{
			add += getPkButton(activeChar.getPkKills());
		}
		
		return add;
	}
	
	private String getPkButton(int i)
	{
		return "<table width=750 height=20><tr><td align=center><button value=\"for " + i + " PK - " + (CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_PRICE * i) + " " + ItemData.getInstance().getTemplate(CommunityServicesConfigs.COMMUNITY_SERVICES_WASH_PK_ID).getName() + " \" action=\"bypass " + _servicesBBSCommand + "_deletePK " + i + "\" width=280 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>";
	}
	
	private String commandSeperator(String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		String dat = st.nextToken();
		return dat;
	}
	
	private class Teleport implements Runnable
	{
		L2PcInstance _activeChar;
		private final int _x, _y, _z;
		
		Teleport(L2PcInstance activeChar, int x, int y, int z)
		{
			_activeChar = activeChar;
			_x = x;
			_y = y;
			_z = z;
		}
		
		@Override
		public void run()
		{
			_activeChar.teleToLocation(_x, _y, _z, true);
			_activeChar.setIsCastingNow(false);
			_activeChar.enableAllSkills();
		}
	}
	
	private void sendHtm(L2PcInstance activeChar, String filepath, String path, String file, String command)
	{
		String content = "";
		filepath = path + file + ".htm";
		File filecom = new File(filepath);
		
		if (!filecom.exists())
		{
			content = "<html><body><br><br><center>The command " + command + " points to file(" + filepath + ") that NOT exists.</center></body></html>";
			separateAndSend(content, activeChar);
			return;
		}
		
		content = HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), filepath);
		if (content.isEmpty())
		{
			content = "<html><body><br><br><center>Content Empty: The command " + command + " points to an invalid or empty html file(" + filepath + ").</center></body></html>";
		}
		
		separateAndSend(content, activeChar);
	}
	
	@Override
	protected void separateAndSend(String html, L2PcInstance acha)
	{
		html = html.replace("\t", "");
		html = html.replace("%command%", _servicesBBSCommand);
		if (html.length() < 8180)
		{
			acha.sendPacket(new ShowBoard(html, "101"));
			acha.sendPacket(new ShowBoard(null, "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 2))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, html.length()), "102"));
			acha.sendPacket(new ShowBoard(null, "103"));
		}
		else if (html.length() < (8180 * 3))
		{
			acha.sendPacket(new ShowBoard(html.substring(0, 8180), "101"));
			acha.sendPacket(new ShowBoard(html.substring(8180, 8180 * 2), "102"));
			acha.sendPacket(new ShowBoard(html.substring(8180 * 2, html.length()), "103"));
		}
	}
	
	@Override
	public void parsewrite(String url, String ar1, String ar2, String ar3, String ar4, String ar5, L2PcInstance activeChar)
	{
		
	}
	
	public static ServicesBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ServicesBBSManager _instance = new ServicesBBSManager();
	}
}