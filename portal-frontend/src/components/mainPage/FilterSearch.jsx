import search from '../../assets/static-images/Лупа для поиска.svg'
import '../../styles/mainPage/FilterSearch.css'

const defaultPlaceholder = 'Поиск по №, теме или автору…'

export default function FilterSearch({ value, onChange, placeholder = defaultPlaceholder }) {
    return (
        <div className="filter-search">
            <div className="search-container">
                <div className="search">
                    <input
                        type="search"
                        className="search-form"
                        value={value}
                        placeholder={placeholder}
                        onChange={(e) => onChange?.(e.target.value)}
                        aria-label={placeholder}
                    />
                    <div className="search-placeholder" aria-hidden>
                        <img src={search} alt="" className="search-icon" />
                    </div>
                </div>
                <button type="button" className="filter-btn">
                    Быстрые фильтры
                    <span className="filter-arrow">▾</span>
                </button>
            </div>
        </div>
    )
}