import FilterSearch from "./FilterSearch"
import TaskTable from "./TaskTable"
import MainDescription from "../MainDescription"

import '../styles/Content.css'

export default function Content() {
    return (
        <div className="content">
            <MainDescription/>
            <FilterSearch />
            <TaskTable />
        </div>
    )
}