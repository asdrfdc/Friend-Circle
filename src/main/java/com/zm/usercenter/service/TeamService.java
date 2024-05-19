package com.zm.usercenter.service;

import com.zm.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zm.usercenter.model.domain.User;
import com.zm.usercenter.model.dto.TeamQuery;
import com.zm.usercenter.model.request.TeamJoinRequest;
import com.zm.usercenter.model.request.TeamQuitRequest;
import com.zm.usercenter.model.request.TeamUpdateRequest;
import com.zm.usercenter.model.vo.TeamUserVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 29524
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-05-11 23:11:45
*/
@Service
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 删除队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id,User loginUser);
}
