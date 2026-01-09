"use client"

import * as React from "react"
import DatePicker from "react-datepicker"
import { es } from "date-fns/locale"
import { CalendarIcon } from "lucide-react"

import { cn } from "@/lib/utils"

interface DatePickerProps {
  value?: string
  onChange: (date: string) => void
  placeholder?: string
  disabled?: boolean
  className?: string
  fromYear?: number
  toYear?: number
}

function DatePickerComponent({
  value,
  onChange,
  placeholder = "Seleccionar fecha",
  disabled = false,
  className,
  fromYear = 1940,
  toYear = new Date().getFullYear(),
}: DatePickerProps) {
  const parseValueToDate = React.useCallback((dateString: string | undefined): Date | null => {
    if (!dateString) return null
    const [year, month, day] = dateString.split('-').map(Number)
    if (isNaN(year) || isNaN(month) || isNaN(day)) return null
    return new Date(Date.UTC(year, month - 1, day, 12, 0, 0))
  }, [])

  const formatDateToString = React.useCallback((date: Date | null): string => {
    if (!date) return ''
    const year = date.getUTCFullYear()
    const month = String(date.getUTCMonth() + 1).padStart(2, '0')
    const day = String(date.getUTCDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }, [])

  const selectedDate = parseValueToDate(value)

  const handleChange = (date: Date | null) => {
    if (!date) return
    const utcDate = new Date(Date.UTC(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      12, 0, 0
    ))
    const formattedDate = formatDateToString(utcDate)
    onChange(formattedDate)
  }

  const minDate = React.useMemo(() => new Date(fromYear, 0, 1), [fromYear])
  const maxDate = React.useMemo(() => new Date(toYear, 11, 31), [toYear])

  return (
    <div className={cn("relative w-full", className)}>
      <DatePicker
        selected={selectedDate}
        onChange={(d: Date | null) => handleChange(d)}
        locale={es}
        dateFormat="PPP"
        placeholderText={placeholder}
        disabled={disabled}
        showYearDropdown
        showMonthDropdown
        dropdownMode="select"
        yearDropdownItemNumber={toYear - fromYear + 1}
        minDate={minDate}
        maxDate={maxDate}
        scrollableYearDropdown
        className={cn(
          "flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 pr-10 text-sm",
          "ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium",
          "placeholder:text-muted-foreground",
          "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2",
          "disabled:cursor-not-allowed disabled:opacity-50"
        )}
        wrapperClassName="w-full"
        popperClassName="react-datepicker-popper"
        popperPlacement="bottom-start"
        popperModifiers={( [
          {
            name: "offset",
            options: {
              offset: [0, 4],
            },
          },
          {
            name: "preventOverflow",
            options: {
              rootBoundary: "viewport",
              padding: 8,
            },
          },
        ] ) as any}
      />
      <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none">
        <CalendarIcon className="h-4 w-4 text-muted-foreground" />
      </div>
    </div>
  )
}

export { DatePickerComponent as DatePicker }
