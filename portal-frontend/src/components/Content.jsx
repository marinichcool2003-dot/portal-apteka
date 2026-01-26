import FilterSearch from "./FilterSearch"
import TaskTable from "./TaskTable"

import '../styles/Content.css'

export default function Content() {
    return (
        <div className="content">
            <div className="description">
                <h1>Управление задачами</h1>
                <h2>Просмотр и управление задачами</h2>
            </div>
            <FilterSearch />
            <TaskTable />
        </div>
    )
}