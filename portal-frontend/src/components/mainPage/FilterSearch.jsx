import { useState } from 'react'

import search from '../assets/Лупа для поиска.svg'

import '../styles/FilterSearch.css'

export default function FilterSearch() {

    const [searchText, setSearchText] = useState('')
    const [filterType, setFilterType] = useState('ALL')

    return (
        <div className="filter-search">
            <div className="search-container">
                <div className="search">
                    <input
                        type="search"
                        className="search-form"
                        value={searchText}
                        placeholder=""
                        onChange={(e) => setSearchText(e.target.value)}
                    />
                    <div className="search-placeholder">
                        <img src={search} alt="Иконка поиска" className="search-icon" />
                        <span className="placeholder-text">Поиск по задачам</span>
                    </div>
                </div>
                <select
                    className="filter-select"
                    value={filterType}
                    onChange={(e) => setFilterType(e.target.value)}
                >
                    <option value="ALL">Все задачи</option>
                    <option value="MY TASKS">Мои задачи</option>
                    <option value="MY CREATED TASKS">Мои составленные задачи</option>
                </select>
            </div>
        </div>
    )
}