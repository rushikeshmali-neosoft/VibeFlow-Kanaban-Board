const fs = require('fs');
const path = require('path');

function generateDrawio(nodes, edges, filename) {
    let xml = `<?xml version="1.0" encoding="UTF-8"?>\n`;
    xml += `<mxfile host="Electron" modified="2026-04-27T12:00:00.000Z" agent="Mozilla/5.0" version="21.1.2" type="device">\n`;
    xml += `  <diagram id="diagram" name="Page-1">\n`;
    xml += `    <mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1600" pageHeight="1200" math="0" shadow="0">\n`;
    xml += `      <root>\n`;
    xml += `        <mxCell id="0" />\n`;
    xml += `        <mxCell id="1" parent="0" />\n`;

    nodes.forEach(n => {
        // XML escape label
        const safeLabel = n.label.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
        let style = n.style || "rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;";
        if (!style.includes('fontColor=')) style += "fontColor=#111111;";
        xml += `        <mxCell id="${n.id}" value="${safeLabel}" style="${style}" vertex="1" parent="1">\n`;
        xml += `          <mxGeometry x="${n.x}" y="${n.y}" width="${n.w || 180}" height="${n.h || 60}" as="geometry" />\n`;
        xml += `        </mxCell>\n`;
    });

    edges.forEach((e, i) => {
        const safeLabel = e.label ? e.label.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;') : '';
        let style = e.style || "edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;";
        if (!style.includes('fontColor=')) style += "fontColor=#111111;labelBackgroundColor=#ffffff;";
        xml += `        <mxCell id="e${i}" value="${safeLabel}" style="${style}" edge="1" parent="1" source="${e.from}" target="${e.to}">\n`;
        xml += `          <mxGeometry relative="1" as="geometry" />\n`;
        xml += `        </mxCell>\n`;
    });

    xml += `      </root>\n`;
    xml += `    </mxGraphModel>\n`;
    xml += `  </diagram>\n`;
    xml += `</mxfile>\n`;

    const outputPath = path.join(__dirname, filename);
    fs.writeFileSync(outputPath, xml);
    console.log(`Generated ${outputPath}`);
}

// STYLES
const STYLE_CONTROLLER = "rounded=1;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;fontColor=#111111;";
const STYLE_SERVICE = "rounded=1;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;fontColor=#111111;";
const STYLE_REPO = "shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=15;fillColor=#ffe6cc;strokeColor=#d79b00;fontColor=#111111;";
const STYLE_DB = "shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=15;fillColor=#f8cecc;strokeColor=#b85450;fontStyle=1;fontColor=#111111;";
const STYLE_CLIENT = "rounded=1;whiteSpace=wrap;html=1;fillColor=#e1d5e7;strokeColor=#9673a6;fontStyle=1;fontColor=#111111;";
const STYLE_UI = "rounded=1;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontColor=#111111;";

// 1. BACKEND FLOW
const backendNodes = [
    { id: 'client', label: 'Frontend Client / Angular', x: 500, y: 50, style: STYLE_CLIENT },

    { id: 'c_auth', label: 'AuthController', x: 100, y: 200, style: STYLE_CONTROLLER },
    { id: 'c_user', label: 'UserController', x: 300, y: 200, style: STYLE_CONTROLLER },
    { id: 'c_task', label: 'TaskController\nAssignmentController\nWorklogController', x: 500, y: 200, style: STYLE_CONTROLLER, h: 80 },
    { id: 'c_admin', label: 'Admin Controllers\n(User, Task, Export)', x: 750, y: 200, style: STYLE_CONTROLLER, h: 80 },
    { id: 'c_report', label: 'ReportController', x: 950, y: 200, style: STYLE_CONTROLLER },

    { id: 's_auth', label: 'AuthService\nJwtTokenProvider', x: 100, y: 400, style: STYLE_SERVICE, h: 80 },
    { id: 's_user', label: 'UserService', x: 300, y: 400, style: STYLE_SERVICE },
    { id: 's_task', label: 'TaskService\nAssignmentService\nWorklogService', x: 500, y: 400, style: STYLE_SERVICE, h: 80 },
    { id: 's_admin', label: 'Admin Services', x: 750, y: 400, style: STYLE_SERVICE },
    { id: 's_report', label: 'ReportService', x: 950, y: 400, style: STYLE_SERVICE },

    { id: 'r_user', label: 'UserRepository', x: 200, y: 600, style: STYLE_REPO },
    { id: 'r_task', label: 'TaskRepository', x: 500, y: 600, style: STYLE_REPO },
    { id: 'r_worklog', label: 'WorklogRepository\nAssignHistoryRepo', x: 800, y: 600, style: STYLE_REPO, h: 80 },

    { id: 'db', label: 'PostgreSQL Database', x: 500, y: 800, style: STYLE_DB }
];

const backendEdges = [
    { from: 'client', to: 'c_auth', label: 'REST API' },
    { from: 'client', to: 'c_user', label: 'REST API' },
    { from: 'client', to: 'c_task', label: 'REST/WS' },
    { from: 'client', to: 'c_admin', label: 'REST API' },
    { from: 'client', to: 'c_report', label: 'REST API' },

    { from: 'c_auth', to: 's_auth' },
    { from: 'c_user', to: 's_user' },
    { from: 'c_task', to: 's_task' },
    { from: 'c_admin', to: 's_admin' },
    { from: 'c_report', to: 's_report' },

    { from: 's_auth', to: 'r_user' },
    { from: 's_user', to: 'r_user' },
    { from: 's_task', to: 'r_task' },
    { from: 's_task', to: 'r_user' },
    { from: 's_task', to: 'r_worklog' },
    { from: 's_admin', to: 'r_user' },
    { from: 's_admin', to: 'r_task' },
    { from: 's_report', to: 'r_task' },
    { from: 's_report', to: 'r_worklog' },

    { from: 'r_user', to: 'db' },
    { from: 'r_task', to: 'db' },
    { from: 'r_worklog', to: 'db' }
];

generateDrawio(backendNodes, backendEdges, '1_Backend_Flow.drawio');

// 2. FRONTEND FLOW
const frontendNodes = [
    { id: 'ui_login', label: 'Auth Components\n(Login, Register, Forgot)', x: 100, y: 100, style: STYLE_UI, h: 80 },
    { id: 'ui_board', label: 'Board Component\nTask Dialog', x: 350, y: 100, style: STYLE_UI, h: 80 },
    { id: 'ui_admin', label: 'Admin Dashboard', x: 600, y: 100, style: STYLE_UI },
    { id: 'ui_report', label: 'Time Reports', x: 800, y: 100, style: STYLE_UI },
    { id: 'ui_nav', label: 'Navbar / Sidebar', x: 450, y: 10, style: STYLE_UI, h: 40 },

    { id: 'guard', label: 'AuthGuard / AdminGuard', x: 450, y: 220, style: "rounded=1;whiteSpace=wrap;html=1;fillColor=#f5f5f5;strokeColor=#666666;dashed=1;" },

    { id: 's_auth', label: 'AuthService', x: 100, y: 350, style: STYLE_SERVICE },
    { id: 's_task', label: 'TaskService', x: 350, y: 350, style: STYLE_SERVICE },
    { id: 's_admin', label: 'Admin API Calls', x: 600, y: 350, style: STYLE_SERVICE },
    { id: 's_report', label: 'ReportService', x: 800, y: 350, style: STYLE_SERVICE },
    { id: 's_ws', label: 'WebSocketService', x: 450, y: 450, style: STYLE_SERVICE },

    { id: 'interceptor', label: 'JwtInterceptor', x: 450, y: 550, style: STYLE_CONTROLLER },

    { id: 'api', label: 'Backend API', x: 450, y: 700, style: STYLE_CLIENT }
];

const frontendEdges = [
    { from: 'ui_login', to: 'guard' },
    { from: 'ui_board', to: 'guard' },
    { from: 'ui_admin', to: 'guard' },

    { from: 'ui_login', to: 's_auth' },
    { from: 'ui_board', to: 's_task' },
    { from: 'ui_admin', to: 's_admin' },
    { from: 'ui_report', to: 's_report' },
    { from: 'ui_nav', to: 's_auth' },

    { from: 's_task', to: 's_ws', label: 'Real-time updates' },

    { from: 's_auth', to: 'interceptor' },
    { from: 's_task', to: 'interceptor' },
    { from: 's_admin', to: 'interceptor' },
    { from: 's_report', to: 'interceptor' },

    { from: 'interceptor', to: 'api', label: 'Adds Bearer Token' },
    { from: 's_ws', to: 'api', label: 'STOMP/WS' }
];

generateDrawio(frontendNodes, frontendEdges, '2_Frontend_Flow.drawio');

// 3. FULL STACK FLOW
const fullStackNodes = [
    { id: 'user', label: 'User / Browser', x: 400, y: 50, style: 'ellipse;whiteSpace=wrap;html=1;fillColor=#ffe6cc;strokeColor=#d79b00;' },

    { id: 'angular', label: 'Angular SPA\n(Frontend)', x: 400, y: 200, style: STYLE_UI, h: 80 },

    { id: 'nginx', label: 'Reverse Proxy / Web Server', x: 400, y: 350, style: 'rounded=0;whiteSpace=wrap;html=1;fillColor=#e1d5e7;strokeColor=#9673a6;' },

    { id: 'spring', label: 'Spring Boot Application\n(Backend API & WS)', x: 400, y: 500, style: STYLE_CONTROLLER, h: 80 },

    { id: 'hibernate', label: 'Hibernate / JPA ORM', x: 400, y: 650, style: STYLE_SERVICE },

    { id: 'pg', label: 'PostgreSQL Database', x: 400, y: 800, style: STYLE_DB }
];

const fullStackEdges = [
    { from: 'user', to: 'angular', label: 'Interacts' },
    { from: 'angular', to: 'nginx', label: 'HTTP/HTTPS / WSS' },
    { from: 'nginx', to: 'spring', label: 'Forwards API calls' },
    { from: 'spring', to: 'hibernate', label: 'Entities Mapping' },
    { from: 'hibernate', to: 'pg', label: 'JDBC/SQL' }
];

generateDrawio(fullStackNodes, fullStackEdges, '3_FullStack_Flow.drawio');

// 4. DB DESIGN
function makeDbTable(name, color, rows) {
    let html = `<table style="width:100%; height:100%; border-collapse:collapse; font-size:14px; font-family:Helvetica,Arial,sans-serif;">`;
    html += `<thead style="background-color:${color}; color:#ffffff;">`;
    html += `<tr><th colspan="2" style="padding:10px; border-bottom:1px solid #ccc; text-align:center; font-weight:bold; font-size:15px; letter-spacing:1px;">${name.toUpperCase()}</th></tr>`;
    html += `</thead><tbody>`;
    rows.forEach((row, idx) => {
        let keyText = '';
        if (row.type === 'PK') keyText = `<span style="color:#d32f2f;font-weight:bold;font-size:12px;">PK</span>`;
        if (row.type === 'FK') keyText = `<span style="color:#1976d2;font-weight:bold;font-size:12px;">FK</span>`;
        let bg = idx % 2 === 0 ? '#fafafa' : '#ffffff';
        html += `<tr style="background-color:${bg};">`;
        html += `<td style="padding:6px 10px; width:30px; text-align:center; border-right:1px solid #eee; border-bottom:1px solid #eee;">${keyText}</td>`;
        html += `<td style="padding:6px 10px; font-weight:${row.type === 'PK' ? 'bold' : 'normal'}; border-bottom:1px solid #eee;">${row.name}</td>`;
        html += `</tr>`;
    });
    html += `</tbody></table>`;
    return html;
}

const dbNodes = [
    {
        id: 't_user',
        label: makeDbTable('users', '#5c6bc0', [
            { name: 'id', type: 'PK' },
            { name: 'email', type: '' },
            { name: 'password_hash', type: '' },
            { name: 'role', type: '' },
            { name: 'is_active', type: '' },
            { name: 'created_at', type: '' }
        ]),
        x: 100, y: 100, w: 260, h: 220,
        style: 'rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#5c6bc0;shadow=1;strokeWidth=2;overflow=hidden;'
    },
    {
        id: 't_task',
        label: makeDbTable('tasks', '#43a047', [
            { name: 'id', type: 'PK' },
            { name: 'title', type: '' },
            { name: 'description', type: '' },
            { name: 'status', type: '' },
            { name: 'column_order', type: '' },
            { name: 'assignee_id', type: 'FK' },
            { name: 'created_at', type: '' }
        ]),
        x: 550, y: 100, w: 260, h: 250,
        style: 'rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#43a047;shadow=1;strokeWidth=2;overflow=hidden;'
    },
    {
        id: 't_worklog',
        label: makeDbTable('worklogs', '#f57c00', [
            { name: 'id', type: 'PK' },
            { name: 'task_id', type: 'FK' },
            { name: 'user_id', type: 'FK' },
            { name: 'hours', type: '' },
            { name: 'log_date', type: '' },
            { name: 'created_at', type: '' }
        ]),
        x: 550, y: 450, w: 260, h: 220,
        style: 'rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#f57c00;shadow=1;strokeWidth=2;overflow=hidden;'
    },
    {
        id: 't_assign',
        label: makeDbTable('assignment_history', '#8e24aa', [
            { name: 'id', type: 'PK' },
            { name: 'task_id', type: 'FK' },
            { name: 'assignee_id', type: 'FK' },
            { name: 'assigned_by_id', type: 'FK' },
            { name: 'assigned_at', type: '' }
        ]),
        x: 100, y: 450, w: 280, h: 190,
        style: 'rounded=0;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#8e24aa;shadow=1;strokeWidth=2;overflow=hidden;'
    }
];

const ERD_EDGE = "edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;html=1;endArrow=ERmandOne;startArrow=ERmany;endSize=15;startSize=15;strokeWidth=2;strokeColor=#666666;";

const dbEdges = [
    { from: 't_task', to: 't_user', label: 'assignee_id -> id', style: ERD_EDGE },
    { from: 't_worklog', to: 't_task', label: 'task_id -> id', style: ERD_EDGE },
    { from: 't_worklog', to: 't_user', label: 'user_id -> id', style: ERD_EDGE },
    { from: 't_assign', to: 't_task', label: 'task_id -> id', style: ERD_EDGE },
    { from: 't_assign', to: 't_user', label: 'assignee_id -> id', style: ERD_EDGE }
];

generateDrawio(dbNodes, dbEdges, '4_DB_Design.drawio');

// 5. SYSTEM DESIGN OVERVIEW
function makeSysComponent(title, subtitle, color, listItems) {
    let listHtml = '';
    if (listItems && listItems.length > 0) {
        listHtml = `<hr style="width:85%; border:none; border-top:1px solid ${color}40; margin:12px 0;">
                    <ul style="text-align:left; font-size:13px; color:#333; padding-left:24px; margin:0; line-height:1.6;">`;
        listItems.forEach(item => { listHtml += `<li>${item}</li>`; });
        listHtml += `</ul>`;
    }

    return `<div style="width:100%; height:100%; box-sizing:border-box; padding:18px; font-family:Helvetica,Arial,sans-serif; display:flex; flex-direction:column; justify-content:center; align-items:center;">
              <b style="font-size:17px; color:${color}; margin-bottom:6px; letter-spacing:0.5px;">${title}</b>
              <span style="font-size:13px; color:#666; font-weight:bold; background-color:${color}15; padding:4px 10px; border-radius:12px;">${subtitle}</span>
              ${listHtml}
            </div>`;
}

const sysNodes = [
    { id: 'actor_user', label: '<b>Normal User</b><br><span style="font-size:11px;color:#666;">(Employees)</span>', x: 50, y: 150, w: 40, h: 80, style: 'shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;fillColor=#fff2cc;strokeColor=#d6b656;strokeWidth=2;' },
    { id: 'actor_admin', label: '<b>Admin User</b><br><span style="font-size:11px;color:#666;">(Managers)</span>', x: 50, y: 350, w: 40, h: 80, style: 'shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;html=1;outlineConnect=0;fillColor=#f8cecc;strokeColor=#b85450;strokeWidth=2;' },

    {
        id: 'frontend',
        label: makeSysComponent('VIBEFLOW FRONTEND', 'Angular 17 SPA', '#d32f2f', ['Kanban Board UI', 'Admin Dashboard', 'RxJS State Mgt', 'Material Design']),
        x: 250, y: 150, w: 240, h: 280,
        style: 'rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#d32f2f;shadow=1;strokeWidth=2;arcSize=4;'
    },

    {
        id: 'backend',
        label: makeSysComponent('VIBEFLOW BACKEND', 'Spring Boot 3.2', '#388e3c', ['Stateless REST APIs', 'WebSockets (STOMP)', 'JWT Authentication', 'Role-Based Access']),
        x: 650, y: 150, w: 260, h: 280,
        style: 'rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#388e3c;shadow=1;strokeWidth=2;arcSize=4;'
    },

    {
        id: 'db_pg',
        label: makeSysComponent('DATABASE', 'PostgreSQL 16', '#1976d2', ['Relational Data', 'JSONB Metadata', 'Transactional Integrity']),
        x: 1050, y: 120, w: 240, h: 180,
        style: 'shape=cylinder3;whiteSpace=wrap;html=1;boundedLbl=1;backgroundOutline=1;size=15;fillColor=#ffffff;strokeColor=#1976d2;shadow=1;strokeWidth=2;'
    },

    {
        id: 'smtp',
        label: makeSysComponent('SMTP SERVER', 'Mail Gateway', '#f57c00', ['Forgot Password', 'OTP Emails']),
        x: 1050, y: 350, w: 240, h: 150,
        style: 'rounded=1;whiteSpace=wrap;html=1;fillColor=#ffffff;strokeColor=#f57c00;shadow=1;strokeWidth=2;arcSize=4;'
    }
];

const SYS_EDGE = "edgeStyle=orthogonalEdgeStyle;rounded=1;orthogonalLoop=1;jettySize=auto;html=1;strokeWidth=2;strokeColor=#555555;fontSize=13;fontStyle=1;labelBackgroundColor=#ffffff;";

const sysEdges = [
    { from: 'actor_user', to: 'frontend', label: 'Uses App', style: SYS_EDGE + "startArrow=none;endArrow=block;endSize=8;" },
    { from: 'actor_admin', to: 'frontend', label: 'Manages System', style: SYS_EDGE + "startArrow=none;endArrow=block;endSize=8;" },
    { from: 'frontend', to: 'backend', label: 'HTTP JSON / WSS', style: SYS_EDGE + "startArrow=classic;endArrow=classic;startSize=8;endSize=8;" },
    { from: 'backend', to: 'db_pg', label: 'JDBC (Port 5432)', style: SYS_EDGE + "startArrow=classic;endArrow=classic;startSize=8;endSize=8;" },
    { from: 'backend', to: 'smtp', label: 'Sends Mails', style: SYS_EDGE + "startArrow=none;endArrow=block;endSize=8;" }
];

generateDrawio(sysNodes, sysEdges, '5_System_Design_Overview.drawio');
