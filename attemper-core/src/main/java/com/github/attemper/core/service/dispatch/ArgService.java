package com.github.attemper.core.service.dispatch;

import com.github.attemper.common.exception.RTException;
import com.github.attemper.common.param.dispatch.arg.ArgListParam;
import com.github.attemper.common.param.dispatch.arg.ArgNameParam;
import com.github.attemper.common.param.dispatch.arg.ArgNamesParam;
import com.github.attemper.common.param.dispatch.arg.ArgSaveParam;
import com.github.attemper.common.param.dispatch.arg.ext.SqlArgParam;
import com.github.attemper.common.param.dispatch.arg.ext.TradeDateArgParam;
import com.github.attemper.common.result.dispatch.arg.Arg;
import com.github.attemper.config.base.datasource.DynamicDataSource;
import com.github.attemper.core.dao.dispatch.ArgMapper;
import com.github.attemper.core.engine.DateCalculatorFactory;
import com.github.attemper.core.engine.date.DateHandler;
import com.github.attemper.core.service.application.GistService;
import com.github.attemper.sys.service.BaseServiceAdapter;
import com.github.attemper.sys.util.PageUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class ArgService extends BaseServiceAdapter {

    @Autowired
    private ArgMapper mapper;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private GistService gistService;

    public Arg get(ArgNameParam param) {
        Map<String, Object> paramMap = injectTenantIdToMap(param);
        return mapper.get(paramMap);
    }

    public Arg add(ArgSaveParam param) {
        Arg arg = get(new ArgNameParam().setArgName(param.getArgName()));
        if (arg != null) {
            throw new DuplicateKeyException(param.getArgName());
        }
        arg = toArg(param);
        mapper.add(arg);
        return arg;
    }

    public void addBatch(List<Arg> args) {
        mapper.addBatch(args);
    }

    public Arg update(ArgSaveParam param) {
        Arg oldArg = get(new ArgNameParam().setArgName(param.getArgName()));
        if (oldArg == null) {
            return add(param);
        }
        Arg updatedArg = toArg(param);
        mapper.update(updatedArg);
        return updatedArg;
    }

    public Map<String, Object> list(ArgListParam param) {
        Map<String, Object> paramMap = injectTenantIdToMap(param);
        PageHelper.startPage(param.getCurrentPage(), param.getPageSize());
        Page<Arg> list = (Page<Arg>) mapper.list(paramMap);
        return PageUtil.toResultMap(list);
    }

    public Void remove(ArgNamesParam param) {
        Map<String, Object> paramMap = injectTenantIdToMap(param);
        mapper.delete(paramMap);
        return null;
    }

    private Arg toArg(ArgSaveParam param) {
        return new Arg()
                .setArgName(param.getArgName())
                .setArgType(param.getArgType())
                .setArgValue(param.getArgValue())
                .setGenericType(param.getGenericType())
                .setAttribute(param.getAttribute())
                .setRemark(param.getRemark())
                .setTenantId(injectTenantId());
    }

    @Autowired
    private DynamicDataSource dynamicDataSource;

    public List<Map<String, Object>> getSqlResult(SqlArgParam param) {
        DataSource targetDataSource;
        if (StringUtils.isNotBlank(param.getDbName())) {
            targetDataSource = dataSourceService.getDataSource(param.getDbName(), injectTenantId());
        } else {
            targetDataSource = dynamicDataSource;
        }
        try {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(targetDataSource);
            return jdbcTemplate.queryForList(param.getSql());
        } catch (Exception e) {
            throw new RTException(1201, e);
        }
    }

    public Integer getTradeDate(TradeDateArgParam param) {
        String expression = param.getExpression().trim();
        DateHandler dateHandler = DateCalculatorFactory.getDateHandler(expression.substring(0, 1));
        dateHandler.setCalendarName(param.getCalendarName());
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine scriptEngine = manager.getEngineByName("js");
        if (expression.length() > 1) {
            String[] arr = expression.substring(1).split(" ");
            try {
                if (arr[0].length() > 1) {
                    dateHandler.setPeriodOffset((Integer) scriptEngine.eval(arr[0].trim()));
                }
                if (arr.length > 1 && arr[1].length() > 1) {
                    dateHandler.setDayOrder((Integer) scriptEngine.eval(arr[1].trim()));
                }
            } catch (ScriptException e) {
                throw new RTException(e);
            }
        }
        return dateHandler.calculateTradeDate();
    }

    public String getGistCode(String gistName, String tenantId) {
        String latestContent = gistService.getLatestContent(gistName, tenantId);
        if (StringUtils.isBlank(latestContent)) {
            log.error("gist code is blank:{}-{}", gistName, tenantId);
        }
        return latestContent;
    }
}
