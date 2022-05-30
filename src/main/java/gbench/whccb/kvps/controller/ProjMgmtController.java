package gbench.whccb.kvps.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gbench.util.lisp.IRecord;
import gbench.whccb.kvps.model.DBModel;

@RequestMapping("pm")
@RestController
public class ProjMgmtController {

    /**
     * 获取项目团队成员信息
     * 
     * http://localhost:8089/kvps/pm/teamGroup
     * 
     * @return 项目团队
     */
    @RequestMapping("teamGroup")
    public Map<String, Object> teamGroup(final String proj_key) {
        final IRecord rec = IRecord.REC("code", 0);
        rec.add("users", dbModel.teamGroup(proj_key));
        return rec.toMap2();
    }

    @Autowired
    private DBModel dbModel;
}
