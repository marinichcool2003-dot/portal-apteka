import { useTasks } from "../../hooks/GetAllTask"

import '../styles/TaskTable.css'

export default function TaskTable() {

    const { tasks, loading, error } = useTasks()

    return (
        <div className="tasks-table-container">
            {loading ? (
                <p>Загрузка...</p>
            ) : error ? (
                <p>{error}</p>
            ) : (
                <table className="tasks-table">
                    <thead>
                        <tr>
                            <th className="task-list-title">№ задачи</th>
                            <th className="task-list-title">Статус</th>
                            <th className="task-list-title">Приоритет</th>
                            <th className="task-list-title">Автор</th>
                            <th className="task-list-title">Тип работ</th>
                            <th className="task-list-title">Вид работ</th>
                            <th className="task-list-title">Тема</th>
                            <th className="task-list-title">Дата</th>
                            <th className="task-list-title">Исполнитель</th>
                        </tr>
                    </thead>
                    <tbody>
                        {tasks.map((task) => (
                            <tr key={task.id}>
                                <td className="task-list-item">{'#' + task.id}</td>
                                <td className="task-list-item">
                                    <span className={`status ${task.status.toLowerCase().replace(' ', '-')}`}>
                                        {(task.status)}
                                    </span>
                                </td>
                                <td>
                                    <span className={`priority ${task.priority.toLowerCase()}`}>
                                        {task.priority}
                                    </span>
                                </td>
                                <td className="task-list-item">{task.aptekaLogin || '—'}</td>
                                <td className="task-list-item">{task.group}</td>
                                <td className="task-list-item">{task.workTask}</td>
                                <td className="task-list-item">{task.title}</td>
                                <td className="task-list-item">{new Date(task.date).toLocaleDateString()}</td>
                                <td className="task-list-item">{task.clientFullName || '—'}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    )
}